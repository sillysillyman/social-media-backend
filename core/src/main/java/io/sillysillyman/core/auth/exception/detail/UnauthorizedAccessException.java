package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.CustomAuthenticationException;

public class UnauthorizedAccessException extends CustomAuthenticationException {

    public UnauthorizedAccessException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
