package io.sillysillyman.socialmediabackend.domain.post;

import io.sillysillyman.socialmediabackend.common.BaseEntity;
import io.sillysillyman.socialmediabackend.domain.post.dto.UpdatePostRequest;
import io.sillysillyman.socialmediabackend.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Post(String content, User user) {
        this.content = content;
        this.user = user;
    }

    public void update(UpdatePostRequest updatePostRequest) {
        if (updatePostRequest.getContent() != null) {
            this.content = updatePostRequest.getContent();
        }
    }
}
