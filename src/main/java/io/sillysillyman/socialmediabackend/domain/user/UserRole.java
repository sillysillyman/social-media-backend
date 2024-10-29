package io.sillysillyman.socialmediabackend.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER");

    private final String authority;
}