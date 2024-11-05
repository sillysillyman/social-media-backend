package io.sillysillyman.core.domain.user.command;

public interface ChangePasswordCommand {

    String getCurrentPassword();

    String getNewPassword();

    String getConfirmNewPassword();
}
