package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.CustomAuthenticationException;

public class AuthenticationFailedException extends CustomAuthenticationException {

    public AuthenticationFailedException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }

    public AuthenticationFailedException(AuthErrorCode authErrorCode, Throwable cause) {
        super(authErrorCode, cause);
    }
}
