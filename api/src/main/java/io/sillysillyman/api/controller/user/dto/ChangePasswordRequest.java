package io.sillysillyman.api.controller.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangePasswordRequest {

    @NotBlank(message = "current password must not be blank")
    String currentPassword;

    @NotBlank(message = "new password must not be blank")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$",
        message = "new password must contain at least one letter, one number, and one special character")
    @Size(min = 8, message = "new password must be at least 8 characters long")
    String newPassword;

    @NotBlank(message = "confirm new password must not be blank")
    String confirmNewPassword;
}
