package io.sillysillyman.core.domain.post;

import io.sillysillyman.core.domain.post.command.UpdatePostCommand;
import io.sillysillyman.core.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Post {

    private final Long id;
    private final User user;
    private String content;

    @Builder
    public Post(Long id, User user, String content) {
        this.id = id;
        this.user = user;
        this.content = content;
    }

    public static Post from(PostEntity postEntity) {
        return Post.builder()
            .id(postEntity.getId())
            .user(User.from(postEntity.getUser()))
            .content(postEntity.getContent())
            .build();
    }

    public void update(UpdatePostCommand updatePostCommand) {
        this.content = updatePostCommand.content();
    }
}
