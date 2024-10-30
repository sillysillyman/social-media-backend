package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageException;

public class TokenSaveFailedException extends TokenStorageException {

    public TokenSaveFailedException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenSaveFailedException(TokenStorageErrorCode tokenStorageErrorCode, Throwable cause) {
        super(tokenStorageErrorCode, cause);
    }
}
