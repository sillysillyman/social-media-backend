package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageException;

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
