package io.sillysillyman.core.domain.user.command;

public interface SignupCommand {

    String getUsername();

    String getPassword();

    String getConfirmPassword();
}
