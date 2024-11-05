package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageException;

public class TokenRetrieveFailedException extends TokenStorageException {

    public TokenRetrieveFailedException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenRetrieveFailedException(
        TokenStorageErrorCode tokenStorageErrorCode,
        Throwable cause
    ) {
        super(tokenStorageErrorCode, cause);
    }
}
