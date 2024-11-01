package io.sillysillyman.socialmediabackend.domain.post.dto;

import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.user.dto.UserResponse;

public record PostResponse(Long postId, String content, UserResponse userResponse) {

    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getContent(), UserResponse.from(post.getUser()));
    }
}
