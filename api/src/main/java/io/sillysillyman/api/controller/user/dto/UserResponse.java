package io.sillysillyman.api.controller.user.dto;

import io.sillysillyman.core.domain.user.User;

public record UserResponse(Long userId, String username) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername());
    }
}
