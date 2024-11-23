package io.sillysillyman.core.domain.comment;

import io.sillysillyman.core.domain.comment.command.UpdateCommentCommand;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Comment {

    private final Long id;
    private final Post post;
    private final User user;
    private String content;

    @Builder
    public Comment(Long id, Post post, User user, String content) {
        this.id = id;
        this.post = post;
        this.user = user;
        this.content = content;
    }

    public static Comment from(CommentEntity commentEntity) {
        return Comment.builder()
            .id(commentEntity.getId())
            .post(Post.from(commentEntity.getPost()))
            .user(User.from(commentEntity.getUser()))
            .content(commentEntity.getContent())
            .build();
    }

    public void update(UpdateCommentCommand updateCommentCommand) {
        this.content = updateCommentCommand.content();
    }
}
