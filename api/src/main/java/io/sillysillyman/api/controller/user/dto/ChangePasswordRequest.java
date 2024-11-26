package io.sillysillyman.api.controller.user.dto;

import io.sillysillyman.core.domain.user.command.ChangePasswordCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public record ChangePasswordRequest(
    @NotBlank(message = "current password must not be blank")
    String currentPassword,

    @NotBlank(message = "new password must not be blank")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
        message = "new password must contain at least one letter, one number, and one special character")
    @Size(min = 8, message = "new password must be at least 8 characters long")
    String newPassword,

    @NotBlank(message = "confirm new password must not be blank")
    String confirmNewPassword
) implements ChangePasswordCommand {

    @AssertTrue(message = "new password must be different from current password")
    private boolean isNewPasswordDifferent() {
        return !Objects.equals(newPassword, currentPassword);
    }

    @AssertTrue(message = "confirm new password must match with new password")
    private boolean isConfirmNewPasswordMatching() {
        return Objects.equals(newPassword, confirmNewPassword);
    }
}
