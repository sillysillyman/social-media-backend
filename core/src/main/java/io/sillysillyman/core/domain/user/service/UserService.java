package io.sillysillyman.core.domain.user.service;

import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserRole;
import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.detail.DuplicateUsernameException;
import io.sillysillyman.core.domain.user.exception.detail.PasswordMismatchException;
import io.sillysillyman.core.domain.user.exception.detail.SamePasswordException;
import io.sillysillyman.core.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.core.domain.user.repository.UserRepository;
import io.sillysillyman.socialmediabackend.domain.user.dto.ChangePasswordRequest;
import io.sillysillyman.socialmediabackend.domain.user.dto.SignupRequest;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserResponse;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {
        validateUsernameUniqueness(signupRequest.getUsername());
        User user = User.builder()
            .username(signupRequest.getUsername())
            .password(passwordEncoder.encode(signupRequest.getPassword()))
            .role(UserRole.USER)
            .build();
        userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return UserResponse.from(getById(userId));
    }

    @Transactional
    public void changePassword(ChangePasswordRequest changePasswordRequest, User user) {
        validateCurrentPasswordMatches(
            user.getPassword(),
            passwordEncoder.encode(changePasswordRequest.getCurrentPassword())
        );
        validateNewPasswordIsDifferent(
            changePasswordRequest.getCurrentPassword(),
            changePasswordRequest.getNewPassword()
        );
        user.changePassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
    }

    @Transactional
    public void withdraw(User user) {
        user.delete();
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateCurrentPasswordMatches(String password, String currentPassword) {
        if (!Objects.equals(password, currentPassword)) {
            throw new PasswordMismatchException(UserErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateNewPasswordIsDifferent(String currentPassword, String newPassword) {
        if (Objects.equals(currentPassword, newPassword)) {
            throw new SamePasswordException(UserErrorCode.SAME_PASSWORD);
        }
    }
}
