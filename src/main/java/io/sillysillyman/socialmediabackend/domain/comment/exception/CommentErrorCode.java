package io.sillysillyman.socialmediabackend.domain.comment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode {
    COMMENT_NOT_BELONG_TO_POST(HttpStatus.BAD_REQUEST, "comment does not belong to the post"),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "comment not found");

    private final HttpStatus status;
    private final String message;
}
