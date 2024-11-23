package io.sillysillyman.core.domain.user.service;

import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserEntity;
import io.sillysillyman.core.domain.user.UserRole;
import io.sillysillyman.core.domain.user.command.ChangePasswordCommand;
import io.sillysillyman.core.domain.user.command.SignupCommand;
import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.detail.DuplicateUsernameException;
import io.sillysillyman.core.domain.user.exception.detail.PasswordMismatchException;
import io.sillysillyman.core.domain.user.exception.detail.SamePasswordException;
import io.sillysillyman.core.domain.user.exception.detail.UserNotFoundException;
import io.sillysillyman.core.domain.user.repository.UserRepository;
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
        return User.from(
            userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND)
            )
        );
    }

    @Transactional
    public User signup(SignupCommand signupCommand) {
        validateUsernameUniqueness(signupCommand.username());
        validateConfirmPasswordMatches(
            signupCommand.password(),
            signupCommand.confirmPassword()
        );

        User user = User.builder()
            .username(signupCommand.username())
            .password(passwordEncoder.encode(signupCommand.password()))
            .role(UserRole.USER)
            .build();

        UserEntity userEntity = userRepository.save(UserEntity.from(user));

        return User.from(userEntity);
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return getById(userId);
    }

    @Transactional
    public void changePassword(ChangePasswordCommand changePasswordCommand, User user) {
        validateCurrentPasswordMatches(
            user.getPassword(),
            changePasswordCommand.currentPassword()
        );
        validateNewPasswordIsDifferent(
            changePasswordCommand.currentPassword(),
            changePasswordCommand.newPassword()
        );
        validateConfirmPasswordMatches(
            changePasswordCommand.newPassword(),
            changePasswordCommand.confirmNewPassword()
        );

        user.changePassword(passwordEncoder.encode(changePasswordCommand.newPassword()));

        userRepository.save(UserEntity.from(user));
    }

    @Transactional
    public void withdraw(User user) {
        user.delete();

        userRepository.save(UserEntity.from(user));
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUsernameException(UserErrorCode.DUPLICATE_USERNAME);
        }
    }

    private void validateCurrentPasswordMatches(String currentPassword, String providedPassword) {
        if (!passwordEncoder.matches(providedPassword, currentPassword)) {
            throw new PasswordMismatchException(UserErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateNewPasswordIsDifferent(String currentPassword, String newPassword) {
        if (Objects.equals(currentPassword, newPassword)) {
            throw new SamePasswordException(UserErrorCode.SAME_PASSWORD);
        }
    }

    private void validateConfirmPasswordMatches(String password, String confirmPassword) {
        if (!Objects.equals(password, confirmPassword)) {
            throw new PasswordMismatchException(UserErrorCode.PASSWORD_MISMATCH);
        }
    }
}
