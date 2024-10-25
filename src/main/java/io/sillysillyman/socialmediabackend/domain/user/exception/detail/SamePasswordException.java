package io.sillysillyman.socialmediabackend.domain.user.exception.detail;

import io.sillysillyman.socialmediabackend.domain.user.exception.UserErrorCode;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;

public class SamePasswordException extends UserException {

    public SamePasswordException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
