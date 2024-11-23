package io.sillysillyman.core.domain.reply;

import io.sillysillyman.core.common.BaseEntity;
import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.user.UserEntity;
import jakarta.persistence.Column;
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
@Table(name = "replies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReplyEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private CommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Builder
    private ReplyEntity(Long id, String content, CommentEntity comment, UserEntity user) {
        this.id = id;
        this.content = content;
        this.comment = comment;
        this.user = user;
    }

    public static ReplyEntity from(Reply reply) {
        return ReplyEntity.builder()
            .id(reply.getId())
            .content(reply.getContent())
            .comment(CommentEntity.from(reply.getComment()))
            .user(UserEntity.from(reply.getUser()))
            .build();
    }
}
