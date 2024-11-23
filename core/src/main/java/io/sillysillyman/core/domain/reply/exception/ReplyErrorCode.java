package io.sillysillyman.core.domain.reply.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReplyErrorCode {
    REPLY_NOT_BELONG_TO_COMMENT(HttpStatus.BAD_REQUEST, "reply does not belong to the comment"),
    REPLY_NOT_FOUND(HttpStatus.NOT_FOUND, "reply not found");

    private final HttpStatus status;
    private final String message;
}
