package io.sillysillyman.core.domain.reply;

import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Reply {

    private final Long id;
    private final Comment comment;
    private final User user;
    private String content;

    @Builder
    public Reply(Long id, Comment comment, User user, String content) {
        this.id = id;
        this.comment = comment;
        this.user = user;
        this.content = content;
    }

    public static Reply from(ReplyEntity replyEntity) {
        return Reply.builder()
            .id(replyEntity.getId())
            .comment(Comment.from(replyEntity.getComment()))
            .user(User.from(replyEntity.getUser()))
            .content(replyEntity.getContent())
            .build();
    }
}
