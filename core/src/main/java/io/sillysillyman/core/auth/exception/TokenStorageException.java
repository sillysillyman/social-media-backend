package io.sillysillyman.core.auth.exception;

import lombok.Getter;

@Getter
public class TokenStorageException extends RuntimeException {

    private final TokenStorageErrorCode tokenStorageErrorCode;

    public TokenStorageException(TokenStorageErrorCode tokenStorageErrorCode) {
        super(tokenStorageErrorCode.getMessage());
        this.tokenStorageErrorCode = tokenStorageErrorCode;
    }

    public TokenStorageException(TokenStorageErrorCode tokenStorageErrorCode, Throwable cause) {
        super(tokenStorageErrorCode.getMessage(), cause);
        this.tokenStorageErrorCode = tokenStorageErrorCode;
    }
}
