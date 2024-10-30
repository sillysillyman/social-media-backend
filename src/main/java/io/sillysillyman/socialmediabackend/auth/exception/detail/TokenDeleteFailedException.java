package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageException;

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
