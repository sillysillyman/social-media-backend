package io.sillysillyman.socialmediabackend.domain.post.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "post not found");

    private final HttpStatus status;
    private final String message;
}
