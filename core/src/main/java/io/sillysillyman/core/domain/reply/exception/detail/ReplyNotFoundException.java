package io.sillysillyman.core.domain.reply.exception.detail;

import io.sillysillyman.core.domain.reply.exception.ReplyErrorCode;
import io.sillysillyman.core.domain.reply.exception.ReplyException;

public class ReplyNotFoundException extends ReplyException {

    public ReplyNotFoundException(ReplyErrorCode replyErrorCode) {
        super(replyErrorCode);
    }
}
