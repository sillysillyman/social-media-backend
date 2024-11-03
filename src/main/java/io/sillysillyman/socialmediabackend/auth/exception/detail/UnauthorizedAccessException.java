package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.CustomAuthenticationException;

public class UnauthorizedAccessException extends CustomAuthenticationException {

    public UnauthorizedAccessException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
