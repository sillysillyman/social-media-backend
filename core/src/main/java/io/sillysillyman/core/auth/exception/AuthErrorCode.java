package io.sillysillyman.core.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "username or password mismatch"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "forbidden access"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "invalid token"),
    UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "unauthenticated user attempt");

    private final HttpStatus status;
    private final String message;
}
