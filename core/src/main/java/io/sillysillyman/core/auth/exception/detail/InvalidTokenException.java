package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.CustomAuthenticationException;

public class InvalidTokenException extends CustomAuthenticationException {

    public InvalidTokenException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }

    public InvalidTokenException(AuthErrorCode authErrorCode, Throwable cause) {
        super(authErrorCode, cause);
    }
}
