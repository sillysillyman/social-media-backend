package io.sillysillyman.socialmediabackend.domain.comment.exception.detail;

import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentException;

public class CommentNotFoundException extends CommentException {

    public CommentNotFoundException(CommentErrorCode commentErrorCode) {
        super(commentErrorCode);
    }
}
