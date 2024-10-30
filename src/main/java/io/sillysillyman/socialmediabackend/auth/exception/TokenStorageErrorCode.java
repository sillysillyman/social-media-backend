package io.sillysillyman.socialmediabackend.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TokenStorageErrorCode {
    REFRESH_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete refresh token"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "refresh token not found"),
    REFRESH_TOKEN_RETRIEVE_FAILED(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "failed to retrieve refresh token"
    ),
    REFRESH_TOKEN_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "failed to save refresh token");

    private final HttpStatus status;
    private final String message;
}
