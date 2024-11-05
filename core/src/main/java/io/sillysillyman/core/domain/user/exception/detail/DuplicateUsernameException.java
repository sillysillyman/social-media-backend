package io.sillysillyman.core.domain.user.exception.detail;

import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.UserException;

public class DuplicateUsernameException extends UserException {

    public DuplicateUsernameException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
