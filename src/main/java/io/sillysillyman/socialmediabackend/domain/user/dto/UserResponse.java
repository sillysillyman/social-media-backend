package io.sillysillyman.socialmediabackend.domain.user.dto;

import io.sillysillyman.socialmediabackend.domain.user.User;

public record UserResponse(Long userId, String username) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
