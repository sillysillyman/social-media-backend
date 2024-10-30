package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.CustomAuthenticationException;

public class AuthenticationFailedException extends CustomAuthenticationException {

    public AuthenticationFailedException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }

    public AuthenticationFailedException(AuthErrorCode authErrorCode, Throwable cause) {
        super(authErrorCode, cause);
    }
}
