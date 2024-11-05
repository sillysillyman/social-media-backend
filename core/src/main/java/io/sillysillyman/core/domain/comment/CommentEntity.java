package io.sillysillyman.core.domain.comment;

import io.sillysillyman.core.common.BaseEntity;
import io.sillysillyman.core.domain.post.PostEntity;
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
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private PostEntity postEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Builder
    public CommentEntity(String content, PostEntity postEntity, UserEntity userEntity) {
        this.content = content;
        this.postEntity = postEntity;
        this.userEntity = userEntity;
    }

    public static CommentEntity from(Comment comment) {
        return CommentEntity.builder()
            .content(comment.getContent())
            .postEntity(PostEntity.from(comment.getPost()))
            .userEntity(UserEntity.from(comment.getUser()))
            .build();
    }
}
