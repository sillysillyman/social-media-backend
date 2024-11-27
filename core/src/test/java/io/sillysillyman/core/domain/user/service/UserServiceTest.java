package io.sillysillyman.core.domain.user.service;

import static io.sillysillyman.core.common.constants.TestConstants.ENCODED_NEW_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.ENCODED_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.INCORRECT_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.NEW_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.USERNAME;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ID;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUserEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserEntity;
import io.sillysillyman.core.domain.user.UserRole;
import io.sillysillyman.core.domain.user.command.ChangePasswordCommand;
import io.sillysillyman.core.domain.user.command.SignupCommand;
import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.detail.DuplicateUsernameException;
import io.sillysillyman.core.domain.user.exception.detail.PasswordMismatchException;
import io.sillysillyman.core.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.core.domain.user.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        userEntity = createUserEntity();
        user = User.from(userEntity);
    }

    @DisplayName("사용자 ID로 조회")
    @Nested
    class GetById {

        @DisplayName("사용자 조회 성공")
        @Test
        void given_ExistingUserId_when_GetById_then_ReturnUser() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(userEntity));

            // when
            User foundUser = userService.getById(USER_ID);

            // then
            assertThat(foundUser)
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(USER_ID);
                    assertThat(user.getUsername()).isEqualTo(USERNAME);
                    assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
                });

            then(userRepository).should().findById(USER_ID);
            then(userRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("사용자 조회 실패")
        @Test
        void given_NonExistentUserId_when_GetById_then_ThrowUserNotFoundException() {
            // given
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> userService.getById(USER_ID);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

            then(userRepository).should().findById(USER_ID);
            then(userRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("회원가입")
    @Nested
    class Signup {

        @DisplayName("회원가입 성공")
        @Test
        void given_ValidCommand_when_Signup_then_ReturnSavedUser() {
            // given
            SignupCommand command = new TestSignupCommand(USERNAME, PASSWORD, PASSWORD);

            given(userRepository.existsByUsername(USERNAME)).willReturn(false);
            given(passwordEncoder.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(userRepository.save(any(UserEntity.class))).willReturn(userEntity);

            // when
            User savedUser = userService.signup(command);

            // then
            assertThat(savedUser)
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(USERNAME);
                    assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
                    assertThat(user.getRole()).isEqualTo(UserRole.USER);
                });

            then(userRepository).should().existsByUsername(USERNAME);
            then(userRepository).should().save(any(UserEntity.class));
            then(userRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("중복된 사용자명 회원가입 실패")
        @Test
        void given_DuplicateUsername_when_Signup_then_ThrowDuplicateUsernameException() {
            // given
            SignupCommand command = new TestSignupCommand(USERNAME, PASSWORD, PASSWORD);

            given(userRepository.existsByUsername(USERNAME)).willReturn(true);

            // when
            ThrowingCallable when = () -> userService.signup(command);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage(UserErrorCode.DUPLICATE_USERNAME.getMessage());

            then(userRepository).should().existsByUsername(USERNAME);
            then(userRepository).shouldHaveNoMoreInteractions();
            then(passwordEncoder).shouldHaveNoInteractions();
        }

        private record TestSignupCommand(
            String username,
            String password,
            String confirmPassword
        ) implements SignupCommand {

        }
    }

    @DisplayName("비밀번호 변경")
    @Nested
    class ChangePassword {

        @DisplayName("비밀번호 변경 성공")
        @Test
        void given_ValidCommand_when_ChangePassword_then_PasswordUpdatedSuccessfully() {
            // given
            ChangePasswordCommand command = new TestChangePasswordCommand(
                PASSWORD,
                NEW_PASSWORD,
                NEW_PASSWORD
            );

            given(passwordEncoder.matches(PASSWORD, user.getPassword())).willReturn(true);
            given(passwordEncoder.encode(NEW_PASSWORD)).willReturn(ENCODED_NEW_PASSWORD);

            // when
            userService.changePassword(command, user);

            // then
            assertThat(user.getPassword()).isEqualTo(ENCODED_NEW_PASSWORD);

            then(passwordEncoder).should().matches(PASSWORD, ENCODED_PASSWORD);
            then(passwordEncoder).should().encode(NEW_PASSWORD);
            then(userRepository).should().save(any(UserEntity.class));
            then(passwordEncoder).shouldHaveNoMoreInteractions();
        }

        @DisplayName("잘못된 현재 비밀번호로 비밀번호 변경 실패")
        @Test
        void given_IncorrectCurrentPassword_when_ChangePassword_then_ThrowPasswordMismatchException() {
            // given
            ChangePasswordCommand command = new TestChangePasswordCommand(
                INCORRECT_PASSWORD,
                NEW_PASSWORD,
                NEW_PASSWORD
            );

            given(passwordEncoder.matches(INCORRECT_PASSWORD, user.getPassword())).willReturn(
                false);

            // when
            ThrowingCallable when = () -> userService.changePassword(command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());

            then(passwordEncoder).should().matches(INCORRECT_PASSWORD, user.getPassword());
            then(passwordEncoder).shouldHaveNoMoreInteractions();
            then(userRepository).shouldHaveNoInteractions();
        }

        private record TestChangePasswordCommand(
            String currentPassword,
            String newPassword,
            String confirmNewPassword
        ) implements ChangePasswordCommand {

        }
    }
}
