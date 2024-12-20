package io.sillysillyman.core.auth.service;

import static io.sillysillyman.core.common.constants.TestConstants.ACCESS_TOKEN;
import static io.sillysillyman.core.common.constants.TestConstants.AUTHORITIES;
import static io.sillysillyman.core.common.constants.TestConstants.INCORRECT_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.NEW_ACCESS_TOKEN;
import static io.sillysillyman.core.common.constants.TestConstants.NEW_REFRESH_TOKEN;
import static io.sillysillyman.core.common.constants.TestConstants.PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.REFRESH_TOKEN;
import static io.sillysillyman.core.common.constants.TestConstants.USERNAME;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUserEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;

import io.sillysillyman.core.auth.CustomUserDetails;
import io.sillysillyman.core.auth.Token;
import io.sillysillyman.core.auth.command.LoginCommand;
import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.detail.AuthenticationFailedException;
import io.sillysillyman.core.auth.exception.detail.InvalidTokenException;
import io.sillysillyman.core.auth.exception.detail.TokenNotFoundException;
import io.sillysillyman.core.auth.repository.RefreshTokenRepository;
import io.sillysillyman.core.auth.util.JwtUtil;
import io.sillysillyman.core.domain.user.UserEntity;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = createUserEntity();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @DisplayName("로그인")
    @Nested
    class Login {

        @DisplayName("올바른 인증 정보로 로그인하면 토큰 반환")
        @Test
        void given_ValidCredentials_when_Login_then_ReturnToken() {
            // given
            LoginCommand command = new TestLoginCommand(USERNAME, PASSWORD);

            CustomUserDetails userDetails = new CustomUserDetails(userEntity);

            given(authenticationManager.authenticate(
                    argThat(auth ->
                        auth.getPrincipal().equals(USERNAME) && auth.getCredentials().equals(PASSWORD)
                    )
                )
            ).willReturn(new UsernamePasswordAuthenticationToken(userDetails, null, AUTHORITIES));
            given(jwtUtil.generateAccessToken(anyString(), any()))
                .willReturn(ACCESS_TOKEN);
            given(jwtUtil.generateRefreshToken(anyString(), any()))
                .willReturn(REFRESH_TOKEN);

            // when
            Token token = authService.login(command);

            // then
            assertThat(token.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(token.refreshToken()).isEqualTo(REFRESH_TOKEN);

            then(refreshTokenRepository).should().save(USERNAME, REFRESH_TOKEN);
        }

        @DisplayName("잘못된 인증 정보로 로그인하면 예외 발생")
        @Test
        void given_InvalidCredentials_when_Login_then_ThrowAuthenticationFailedException() {
            // given
            LoginCommand command = new TestLoginCommand(USERNAME, INCORRECT_PASSWORD);

            given(authenticationManager.authenticate(any(Authentication.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

            // when
            ThrowingCallable when = () -> authService.login(command);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage(AuthErrorCode.AUTHENTICATION_FAILED.getMessage());
        }

        private record TestLoginCommand(String username, String password) implements LoginCommand {

        }
    }

    @DisplayName("로그아웃")
    @Nested
    class Logout {

        private CustomUserDetails userDetails;
        private Authentication authentication;
        private SecurityContext securityContext;

        @BeforeEach
        void setUp() {
            userDetails = mock(CustomUserDetails.class);
            authentication = mock(Authentication.class);
            securityContext = mock(SecurityContext.class);
        }

        @DisplayName("로그아웃 성공")
        @Test
        void given_AuthenticatedUser_when_Logout_then_ClearSecurityContextAndDeleteRefreshToken() {
            // given
            given(userDetails.getUsername()).willReturn(USERNAME);
            given(authentication.getPrincipal()).willReturn(userDetails);
            given(securityContext.getAuthentication()).willReturn(authentication);

            SecurityContextHolder.setContext(securityContext);

            // when
            authService.logout();

            // then
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

            then(refreshTokenRepository).should().deleteByUsername(USERNAME);
        }
    }

    @DisplayName("토큰 재발급")
    @Nested
    class Refresh {

        @DisplayName("유효한 리프레시 토큰으로 새로운 토큰 발급")
        @Test
        void given_ValidRefreshToken_when_Refresh_then_GenerateNewTokens() {
            // given
            given(jwtUtil.isTokenValid(REFRESH_TOKEN)).willReturn(true);
            given(jwtUtil.extractUsername(REFRESH_TOKEN)).willReturn(USERNAME);
            given(refreshTokenRepository.findByUsername(USERNAME))
                .willReturn(Optional.of(REFRESH_TOKEN));
            given(jwtUtil.generateAccessToken(USERNAME, AUTHORITIES)).willReturn(NEW_ACCESS_TOKEN);
            given(jwtUtil.generateRefreshToken(USERNAME, AUTHORITIES))
                .willReturn(NEW_REFRESH_TOKEN);
            doReturn(AUTHORITIES).when(jwtUtil).extractAuthorities(REFRESH_TOKEN);

            // when
            Token newToken = authService.refresh(REFRESH_TOKEN);

            // then
            assertThat(newToken.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
            assertThat(newToken.refreshToken()).isEqualTo(NEW_REFRESH_TOKEN);

            then(refreshTokenRepository).should().deleteByUsername(USERNAME);
            then(refreshTokenRepository).should().save(USERNAME, NEW_REFRESH_TOKEN);
        }

        @DisplayName("유효하지 않은 리프레시 토큰으로 요청하면 예외 발생")
        @Test
        void given_InvalidRefreshToken_when_Refresh_then_ThrowInvalidTokenException() {
            // given
            given(jwtUtil.isTokenValid(REFRESH_TOKEN)).willReturn(false);

            // when
            ThrowingCallable when = () -> authService.refresh(REFRESH_TOKEN);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage(AuthErrorCode.INVALID_TOKEN.getMessage());
        }

        @DisplayName("저장소에 리프레시 토큰이 없으면 예외 발생")
        @Test
        void given_MissingRefreshToken_when_Refresh_then_ThrowTokenNotFoundException() {
            // given
            given(jwtUtil.isTokenValid(REFRESH_TOKEN)).willReturn(true);
            given(jwtUtil.extractUsername(REFRESH_TOKEN)).willReturn(USERNAME);
            given(refreshTokenRepository.findByUsername(USERNAME)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> authService.refresh(REFRESH_TOKEN);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(TokenNotFoundException.class)
                .hasMessage(TokenStorageErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }
    }
}
