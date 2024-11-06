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
        validateUsernameUniqueness(signupCommand.getUsername());

        User user = User.builder()
            .username(signupCommand.getUsername())
            .password(passwordEncoder.encode(signupCommand.getPassword()))
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
            changePasswordCommand.getCurrentPassword()
        );
        validateNewPasswordIsDifferent(
            changePasswordCommand.getCurrentPassword(),
            changePasswordCommand.getNewPassword()
        );
        validateConfirmNewPasswordMatches(
            changePasswordCommand.getNewPassword(),
            changePasswordCommand.getConfirmNewPassword()
        );

        user.changePassword(passwordEncoder.encode(changePasswordCommand.getNewPassword()));

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

    private void validateConfirmNewPasswordMatches(String newPassword, String confirmNewPassword) {
        if (!Objects.equals(newPassword, confirmNewPassword)) {
            throw new PasswordMismatchException(UserErrorCode.PASSWORD_MISMATCH);
        }
    }
}
