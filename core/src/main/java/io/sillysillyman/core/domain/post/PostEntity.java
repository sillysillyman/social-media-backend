package io.sillysillyman.core.domain.post;

import io.sillysillyman.core.common.BaseEntity;
import io.sillysillyman.core.domain.user.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Builder
    private PostEntity(Long id, String content, UserEntity user) {
        this.id = id;
        this.content = content;
        this.user = user;
    }

    public static PostEntity from(Post post) {
        return PostEntity.builder()
            .id(post.getId())
            .content(post.getContent())
            .user(UserEntity.from(post.getUser()))
            .build();
    }
}
