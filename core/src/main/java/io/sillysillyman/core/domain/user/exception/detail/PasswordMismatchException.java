package io.sillysillyman.core.domain.user.exception.detail;

import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.UserException;

public class PasswordMismatchException extends UserException {

    public PasswordMismatchException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
