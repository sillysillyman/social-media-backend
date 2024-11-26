package io.sillysillyman.api.controller.user.dto;

import io.sillysillyman.core.domain.user.command.SignupCommand;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public record SignupRequest(
    @NotBlank(message = "username must not be blank")
    String username,

    @NotBlank(message = "password must not be blank")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "password must contain at least one letter, one number, and one special character")
    @Size(min = 8, message = "password must be at least 8 characters long")
    String password,

    @NotBlank(message = "confirm password must not be blank")
    String confirmPassword
) implements SignupCommand {

    @AssertTrue(message = "confirm new password must match with new password")
    private boolean isConfirmPasswordMatching() {
        return Objects.equals(password, confirmPassword);
    }
}
