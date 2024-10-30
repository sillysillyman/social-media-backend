package io.sillysillyman.socialmediabackend.auth.exception.detail;

import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.TokenStorageException;

public class TokenNotFoundException extends TokenStorageException {

    public TokenNotFoundException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode);
    }

    public TokenNotFoundException(TokenStorageErrorCode tokenStorageErrorCode, Throwable cause) {
        super(tokenStorageErrorCode, cause);
    }
}
