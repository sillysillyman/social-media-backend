package io.sillysillyman.socialmediabackend.domain.user.exception.detail;

import io.sillysillyman.socialmediabackend.domain.user.exception.UserErrorCode;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;

public class PasswordMismatchException extends UserException {

    public PasswordMismatchException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
