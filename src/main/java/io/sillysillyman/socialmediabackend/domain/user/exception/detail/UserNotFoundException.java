package io.sillysillyman.socialmediabackend.domain.user.exception.detail;

import io.sillysillyman.socialmediabackend.domain.user.exception.UserErrorCode;
import io.sillysillyman.socialmediabackend.domain.user.exception.UserException;

public class UserNotFoundException extends UserException {

    public UserNotFoundException(UserErrorCode userErrorCode) {
        super(userErrorCode);
    }
}
