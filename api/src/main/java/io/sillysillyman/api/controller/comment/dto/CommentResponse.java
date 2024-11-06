package io.sillysillyman.api.controller.comment.dto;

import io.sillysillyman.api.controller.post.dto.PostResponse;
import io.sillysillyman.api.controller.user.dto.UserResponse;
import io.sillysillyman.core.domain.comment.Comment;

public record CommentResponse(
    Long commentId,
    String content,
    PostResponse postResponse,
    UserResponse userResponse
) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getContent(),
            PostResponse.from(comment.getPost()),
            UserResponse.from(comment.getUser())
        );
    }
}
