package io.sillysillyman.socialmediabackend.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "duplicate username"),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "password mismatch"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "same password"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user not found");

    private final HttpStatus status;
    private final String message;
}
