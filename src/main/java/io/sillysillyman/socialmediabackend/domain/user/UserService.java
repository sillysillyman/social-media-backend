package io.sillysillyman.socialmediabackend.domain.user;

import io.sillysillyman.socialmediabackend.domain.user.dto.ChangePasswordDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.SignupDto;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserDto;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserErrorCode;
import io.sillysillyman.socialmediabackend.domain.user.exception.detail.DuplicateUsernameException;
import io.sillysillyman.socialmediabackend.domain.user.exception.detail.PasswordMismatchException;
import io.sillysillyman.socialmediabackend.domain.user.exception.detail.SamePasswordException;
import io.sillysillyman.socialmediabackend.domain.user.exception.detail.UserNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserDto signup(SignupDto signupDto) {
        validateUsernameUniqueness(signupDto.getUsername());
        User user = User.builder()
            .username(signupDto.getUsername())
            .password(passwordEncoder.encode(signupDto.getPassword()))
            .build();
        userRepository.save(user);
        return UserDto.from(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        User user = getById(userId);
        validateCurrentPasswordMatches(
            user.getPassword(),
            passwordEncoder.encode(changePasswordDto.getCurrentPassword()));
        validateNewPasswordIsDifferent(
            changePasswordDto.getCurrentPassword(),
            changePasswordDto.getNewPassword());
        user.changePassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
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
