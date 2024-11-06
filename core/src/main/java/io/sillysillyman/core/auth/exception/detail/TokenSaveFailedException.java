package io.sillysillyman.core.auth.exception.detail;

import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.TokenStorageException;

public class TokenSaveFailedException extends TokenStorageException {

    public TokenSaveFailedException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenSaveFailedException(TokenStorageErrorCode tokenStorageErrorCode, Throwable cause) {
        super(tokenStorageErrorCode, cause);
    }
}
