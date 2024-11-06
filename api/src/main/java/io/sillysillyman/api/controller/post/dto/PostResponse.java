package io.sillysillyman.api.controller.post.dto;

import io.sillysillyman.api.controller.user.dto.UserResponse;
import io.sillysillyman.core.domain.post.Post;

public record PostResponse(Long postId, String content, UserResponse userResponse) {

    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getContent(), UserResponse.from(post.getUser()));
    }
}
