package io.sillysillyman.core.domain.reply.exception.detail;

import io.sillysillyman.core.domain.reply.exception.ReplyErrorCode;
import io.sillysillyman.core.domain.reply.exception.ReplyException;

public class ReplyNotBelongToCommentException extends ReplyException {

    public ReplyNotBelongToCommentException(ReplyErrorCode replyErrorCode) {
        super(replyErrorCode);
    }
}
