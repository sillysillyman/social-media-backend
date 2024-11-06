package io.sillysillyman.core.domain.comment.exception.detail;

import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.CommentException;

public class CommentNotFoundException extends CommentException {

    public CommentNotFoundException(CommentErrorCode commentErrorCode) {
        super(commentErrorCode);
    }
}
