package io.sillysillyman.socialmediabackend.domain.comment.dto;

import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserResponse;

public record CommentResponse(
    Long commentId,
    String content,
    PostResponse postResponse,
    UserResponse userResponse
) {

}
