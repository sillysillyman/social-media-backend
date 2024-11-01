package io.sillysillyman.socialmediabackend.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "username or password mismatch"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid token"),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "unauthorized access");

    private final HttpStatus status;
    private final String message;
}
