package io.sillysillyman.api.controller.auth.dto;

import io.sillysillyman.core.auth.command.LoginCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LoginRequest implements LoginCommand {

    @NotBlank(message = "username must not be blank")
    String username;

    @NotBlank(message = "password must not be blank")
    String password;
}
