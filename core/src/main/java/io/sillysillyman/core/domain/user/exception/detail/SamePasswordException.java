package io.sillysillyman.core.domain.user.exception.detail;

import io.sillysillyman.core.domain.user.exception.UserErrorCode;
import io.sillysillyman.core.domain.user.exception.UserException;

public class SamePasswordException extends UserException {

    public SamePasswordException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
