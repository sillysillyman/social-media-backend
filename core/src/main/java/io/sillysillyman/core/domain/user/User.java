package io.sillysillyman.core.domain.user;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class User {

    private final Long id;
    private final String username;
    private final UserRole role;
    private String password;
    private Instant deletedAt;

    @Builder
    public User(Long id, String username, String password, UserRole role, Instant deletedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.deletedAt = deletedAt;
    }

    public static User from(UserEntity userEntity) {
        return User.builder()
            .id(userEntity.getId())
            .username(userEntity.getUsername())
            .password(userEntity.getPassword())
            .role(userEntity.getRole())
            .deletedAt(userEntity.getDeletedAt())
            .build();
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}
