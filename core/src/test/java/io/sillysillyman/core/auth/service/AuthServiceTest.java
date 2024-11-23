package io.sillysillyman.core.auth.service;

import static io.sillysillyman.core.domain.user.UserRole.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

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
import io.sillysillyman.core.domain.user.UserRole;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String WRONG_PASSWORD = "wrong_password";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String NEW_ACCESS_TOKEN = "new_access_token";
    private static final String NEW_REFRESH_TOKEN = "new_refresh_token";
    private static final UserRole USER_ROLE = USER;
    private static final Collection<? extends GrantedAuthority> AUTHORITIES =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

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
        userEntity = UserEntity.builder()
            .username(USERNAME)
            .password(PASSWORD)
            .role(USER_ROLE)
            .build();
    }

    @Nested
    @DisplayName("로그인")
    class LoginTest {

        @Test
        @DisplayName("올바른 인증 정보로 로그인하면 토큰 반환")
        void given_ValidCredentials_when_Login_thenReturnToken() {
            // given
            LoginCommand command = new LoginCommand() {
                @Override
                public String username() {
                    return USERNAME;
                }

                @Override
                public String password() {
                    return PASSWORD;
                }
            };

            CustomUserDetails userDetails = new CustomUserDetails(userEntity);
            Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, AUTHORITIES);

            given(authenticationManager.authenticate(any(Authentication.class)))
                .willReturn(authentication);
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

        @Test
        @DisplayName("잘못된 인증 정보로 로그인하면 예외 발생")
        void given_InvalidCredentials_when_Login_then_ThrowAuthenticationFailedException() {
            // given
            LoginCommand command = new LoginCommand() {
                @Override
                public String username() {
                    return USERNAME;
                }

                @Override
                public String password() {
                    return WRONG_PASSWORD;
                }
            };

            given(authenticationManager.authenticate(any(Authentication.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

            // when
            ThrowingCallable when = () -> authService.login(command);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage(AuthErrorCode.AUTHENTICATION_FAILED.getMessage());
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 성공")
        void given_AuthenticatedUser_when_Logout_then_ClearSecurityContextAndDeleteRefreshToken() {
            // given
            CustomUserDetails userDetails = mock(CustomUserDetails.class);
            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

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

    @Nested
    @DisplayName("토큰 재발급")
    class RefreshTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 새로운 토큰 발급")
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

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 요청하면 예외 발생")
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

        @Test
        @DisplayName("저장소에 리프레시 토큰이 없으면 예외 발생")
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
