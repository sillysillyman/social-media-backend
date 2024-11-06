package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageException;

public class TokenDeleteFailedException extends TokenStorageException {

    public TokenDeleteFailedException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenDeleteFailedException(
        TokenStorageErrorCode tokenStorageErrorCode,
        Throwable cause
    ) {
        super(tokenStorageErrorCode, cause);
    }
}
