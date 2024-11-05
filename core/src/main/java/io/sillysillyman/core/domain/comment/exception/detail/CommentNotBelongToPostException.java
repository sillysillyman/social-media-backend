package io.sillysillyman.core.domain.comment.exception.detail;

import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.CommentException;

public class CommentNotBelongToPostException extends CommentException {

    public CommentNotBelongToPostException(CommentErrorCode commentErrorCode) {
        super(commentErrorCode);
    }
}
