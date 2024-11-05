package io.sillysillyman.core.domain.user;

import io.sillysillyman.core.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    private Instant deletedAt;

    @Builder
    public UserEntity(String username, String password, UserRole role, Instant deletedAt) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.deletedAt = deletedAt;
    }

    public static UserEntity from(User user) {
        return UserEntity.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .role(user.getRole())
            .deletedAt(user.getDeletedAt())
            .build();
    }
}
