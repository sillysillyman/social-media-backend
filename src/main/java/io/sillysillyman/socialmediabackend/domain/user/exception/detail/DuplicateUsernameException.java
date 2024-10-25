package io.sillysillyman.socialmediabackend.domain.user.exception.detail;

import io.sillysillyman.socialmediabackend.domain.user.exception.UserErrorCode;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;

public class DuplicateUsernameException extends UserException {

    public DuplicateUsernameException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
