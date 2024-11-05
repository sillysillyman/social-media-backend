package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageException;

public class TokenNotFoundException extends TokenStorageException {

    public TokenNotFoundException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenNotFoundException(TokenStorageErrorCode tokenStorageErrorCode, Throwable cause) {
        super(tokenStorageErrorCode, cause);
    }
}
