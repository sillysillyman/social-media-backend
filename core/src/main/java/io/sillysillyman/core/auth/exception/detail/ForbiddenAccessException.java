package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.CustomAuthenticationException;

public class ForbiddenAccessException extends CustomAuthenticationException {

    public ForbiddenAccessException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
    }
}
