package io.sillysillyman.core.domain.user.service;

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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password1!";
    private static final String ENCODED_PASSWORD = "encodedPassword1!";
    private static final Long DEFAULT_ID = 1L;

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
        userEntity = UserEntity.builder()
            .username(USERNAME)
            .password(ENCODED_PASSWORD)
            .role(UserRole.USER)
            .build();
        ReflectionTestUtils.setField(userEntity, "id", DEFAULT_ID);

        user = User.from(userEntity);
    }

    @DisplayName("사용자 ID로 조회")
    @Nested
    class GetById {

        @DisplayName("존재하는 사용자 ID로 조회하면 사용자 반환")
        @Test
        void given_ExistingUserId_when_GetById_then_ReturnUser() {
            // given
            given(userRepository.findById(DEFAULT_ID)).willReturn(Optional.of(userEntity));

            // when
            User foundUser = userService.getById(DEFAULT_ID);

            // then
            assertThat(foundUser)
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(DEFAULT_ID);
                    assertThat(user.getUsername()).isEqualTo(USERNAME);
                    assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
                });

            then(userRepository).should().findById(DEFAULT_ID);
            then(userRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("존재하지 않는 사용자 ID로 조회하면 예외 발생")
        @Test
        void given_NonExistentUserId_when_GetById_then_ThrowUserNotFoundException() {
            // given
            given(userRepository.findById(DEFAULT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> userService.getById(DEFAULT_ID);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());

            then(userRepository).should().findById(DEFAULT_ID);
            then(userRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("회원가입")
    @Nested
    class Signup {

        @DisplayName("고유한 사용자명으로 가입 시도하면 가입 성공")
        @Test
        void given_UniqueUsername_when_Signup_then_ReturnSavedUser() {
            // given
            SignupCommand command = new SignupCommand() {
                @Override
                public String username() {
                    return USERNAME;
                }

                @Override
                public String password() {
                    return PASSWORD;
                }

                @Override
                public String confirmPassword() {
                    return PASSWORD;
                }
            };

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

        @DisplayName("중복된 사용자명으로 가입 시도하면 예외 발생")
        @Test
        void given_DuplicateUsername_when_Signup_then_ThrowDuplicateUsernameException() {
            // given
            SignupCommand command = new SignupCommand() {
                @Override
                public String username() {
                    return USERNAME;
                }

                @Override
                public String password() {
                    return PASSWORD;
                }

                @Override
                public String confirmPassword() {
                    return PASSWORD;
                }
            };

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
    }

    @DisplayName("비밀번호 변경")
    @Nested
    class ChangePassword {

        private static final String NEW_PASSWORD = "newPassword1!";
        private static final String ENCODED_NEW_PASSWORD = "encodedNewPassword1!";

        @DisplayName("올바른 현재 비밀번호로 변경 시도하면 변경 성공")
        @Test
        void given_ValidCurrentPassword_when_ChangePassword_then_UpdateSuccessfully() {
            // given
            ChangePasswordCommand command = new ChangePasswordCommand() {
                @Override
                public String currentPassword() {
                    return PASSWORD;
                }

                @Override
                public String newPassword() {
                    return NEW_PASSWORD;
                }

                @Override
                public String confirmNewPassword() {
                    return NEW_PASSWORD;
                }
            };

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

        @DisplayName("잘못된 현재 비밀번호로 변경 시도하면 예외 발생")
        @Test
        void given_IncorrectCurrentPassword_when_ChangePassword_then_ThrowPasswordMismatchException() {
            // given
            String incorrectPassword = "incorrectPassword";
            ChangePasswordCommand command = new ChangePasswordCommand() {
                @Override
                public String currentPassword() {
                    return incorrectPassword;
                }

                @Override
                public String newPassword() {
                    return NEW_PASSWORD;
                }

                @Override
                public String confirmNewPassword() {
                    return NEW_PASSWORD;
                }
            };

            given(passwordEncoder.matches(incorrectPassword, user.getPassword())).willReturn(false);

            // when
            ThrowingCallable when = () -> userService.changePassword(command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(PasswordMismatchException.class)
                .hasMessage(UserErrorCode.PASSWORD_MISMATCH.getMessage());

            then(passwordEncoder).should().matches(incorrectPassword, user.getPassword());
            then(passwordEncoder).shouldHaveNoMoreInteractions();
            then(userRepository).shouldHaveNoInteractions();
        }
    }
}
