package io.sillysillyman.core.domain.reply.exception;

import lombok.Getter;

@Getter
public class ReplyException extends RuntimeException {

    private final ReplyErrorCode replyErrorCode;

    public ReplyException(ReplyErrorCode replyErrorCode) {
        super(replyErrorCode.getMessage());
        this.replyErrorCode = replyErrorCode;
    }
}
