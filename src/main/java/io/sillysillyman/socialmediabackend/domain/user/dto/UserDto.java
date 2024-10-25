package io.sillysillyman.socialmediabackend.domain.user.dto;

import io.sillysillyman.socialmediabackend.domain.user.User;

public record UserDto(Long id, String username) {

    public static UserDto from(User user) {
        return new UserDto(user.getId(), user.getUsername());
    }
}
