package io.sillysillyman.socialmediabackend.domain.comment.exception;

import lombok.Getter;

@Getter
public class CommentException extends RuntimeException {

    private final CommentErrorCode commentErrorCode;

    public CommentException(CommentErrorCode commentErrorCode) {
        super(commentErrorCode.getMessage());
        this.commentErrorCode = commentErrorCode;
    }
}
