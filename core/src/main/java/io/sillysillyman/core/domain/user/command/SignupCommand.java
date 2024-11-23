package io.sillysillyman.core.domain.user.command;

public interface SignupCommand {

    String username();

    String password();

    String confirmPassword();
}
