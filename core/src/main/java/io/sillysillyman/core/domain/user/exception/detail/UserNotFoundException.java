package io.sillysillyman.core.domain.user.exception.detail;

import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.UserException;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
