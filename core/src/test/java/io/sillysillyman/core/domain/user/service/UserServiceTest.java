package io.sillysillyman.core.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserRole;
import io.sillysillyman.core.domain.user.exception.detail.DuplicateUsernameException;
import io.sillysillyman.core.domain.user.exception.detail.PasswordMismatchException;
import io.sillysillyman.core.domain.user.exception.detail.SamePasswordException;
import io.sillysillyman.core.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.core.domain.user.repository.UserRepository;
import io.sillysillyman.socialmediabackend.domain.user.dto.ChangePasswordRequest;
import io.sillysillyman.socialmediabackend.domain.user.dto.SignupRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getById_UserExists_ReturnsUser() {
        User user = new User("username", "encodedPassword1!", UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getById(1L);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("username");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getById_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(1L))
            .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void signup_UniqueUsername_SavesUser() {
        SignupRequest signupRequest = createSignupDto("username", "password1!", "password1!");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword1!");

        userService.signup(signupRequest);

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(signupRequest.getPassword());
    }

    @Test
    void signup_DuplicateUsername_ThrowsDuplicateUsernameException() {
        SignupRequest signupRequest = createSignupDto("duplicateUsername", "password1!",
            "password1!");

        when(userRepository.existsByUsername(signupRequest.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(signupRequest))
            .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_SuccessfulChange() {
        User user = new User("username", "encodedOldPassword1!", UserRole.USER);
        ChangePasswordRequest changePasswordRequest = createChangePasswordDto("oldPassword1!",
            "newPassword1!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("oldPassword1!")).thenReturn("encodedOldPassword1!");
        when(passwordEncoder.encode("newPassword1!")).thenReturn("encodedNewPassword1!");

        userService.changePassword(changePasswordRequest, user);

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("oldPassword1!");
        verify(passwordEncoder, times(1)).encode("newPassword1!");
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword1!");
    }

    @Test
    void changePassword_IncorrectCurrentPassword_ThrowsPasswordMismatchException() {
        User user = new User("username", "encodedOldPassword1!", UserRole.USER);
        ChangePasswordRequest changePasswordRequest = createChangePasswordDto("incorrectPassword",
            "newPassword1!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("incorrectPassword")).thenReturn("incorrectEncodedPassword");

        assertThatThrownBy(() -> userService.changePassword(changePasswordRequest, user))
            .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("incorrectPassword");
        verify(passwordEncoder, never()).encode("newPassword!");
    }

    @Test
    void changePassword_SameNewPassword_ThrowsSamePasswordException() {
        User user = new User("username", "encodedOldPassword1!", UserRole.USER);
        ChangePasswordRequest changePasswordRequest = createChangePasswordDto("oldPassword1!",
            "oldPassword1!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("oldPassword1!")).thenReturn("encodedOldPassword1!");

        assertThatThrownBy(() ->
            userService.changePassword(changePasswordRequest, user)).isInstanceOf(
            SamePasswordException.class
        );

        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("oldPassword1!");
    }

    private SignupRequest createSignupDto(String username, String password,
        String confirmPassword) {
        SignupRequest signupRequest = new SignupRequest();
        ReflectionTestUtils.setField(signupRequest, "username", username);
        ReflectionTestUtils.setField(signupRequest, "password", password);
        ReflectionTestUtils.setField(signupRequest, "confirmPassword", confirmPassword);
        return signupRequest;
    }

    private ChangePasswordRequest createChangePasswordDto(String currentPassword,
        String newPassword) {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        ReflectionTestUtils.setField(changePasswordRequest, "currentPassword", currentPassword);
        ReflectionTestUtils.setField(changePasswordRequest, "newPassword", newPassword);
        return changePasswordRequest;
    }
}