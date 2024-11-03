package io.sillysillyman.socialmediabackend.domain.comment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode {
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "comment not found");

    private final HttpStatus status;
    private final String message;
}
