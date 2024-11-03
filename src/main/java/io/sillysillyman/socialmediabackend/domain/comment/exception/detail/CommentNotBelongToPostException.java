package io.sillysillyman.socialmediabackend.domain.comment.exception.detail;

import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentException;

public class CommentNotBelongToPostException extends CommentException {

    public CommentNotBelongToPostException(CommentErrorCode commentErrorCode) {
        super(commentErrorCode);
    }
}
