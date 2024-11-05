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
public class User extends BaseEntity {

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
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void delete() {
        this.deletedAt = Instant.now();
    }
}
