package io.sillysillyman.core.domain.user.command;

public interface ChangePasswordCommand {

    String currentPassword();

    String newPassword();

    String confirmNewPassword();
}
