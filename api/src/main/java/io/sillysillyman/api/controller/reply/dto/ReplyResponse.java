package io.sillysillyman.api.controller.reply.dto;

import io.sillysillyman.api.controller.comment.dto.CommentResponse;
import io.sillysillyman.api.controller.user.dto.UserResponse;
import io.sillysillyman.core.domain.reply.Reply;

public record ReplyResponse(
    Long replyId,
    String content,
    CommentResponse commentResponse,
    UserResponse userResponse
) {

    public static ReplyResponse from(Reply reply) {
        return new ReplyResponse(
            reply.getId(),
            reply.getContent(),
            CommentResponse.from(reply.getComment()),
            UserResponse.from(reply.getUser())
        );
    }
}
