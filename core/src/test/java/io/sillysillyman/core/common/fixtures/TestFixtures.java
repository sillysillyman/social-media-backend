package io.sillysillyman.core.common.fixtures;

import static io.sillysillyman.core.common.constants.TestConstants.COMMENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.ENCODED_PASSWORD;
import static io.sillysillyman.core.common.constants.TestConstants.POST_ID;
import static io.sillysillyman.core.common.constants.TestConstants.REPLY_ID;
import static io.sillysillyman.core.common.constants.TestConstants.UNAUTHORIZED_USERNAME;
import static io.sillysillyman.core.common.constants.TestConstants.UNAUTHORIZED_USER_ID;
import static io.sillysillyman.core.common.constants.TestConstants.USERNAME;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ID;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ROLE;

import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.reply.ReplyEntity;
import io.sillysillyman.core.domain.user.UserEntity;
import java.time.Instant;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static UserEntity createUserEntity() {
        return createUserEntity(USERNAME);
    }

    public static UserEntity createUserEntity(String username) {
        UserEntity userEntity = UserEntity.builder()
            .username(username)
            .password(ENCODED_PASSWORD)
            .role(USER_ROLE)
            .build();
        ReflectionTestUtils.setField(userEntity, "id", USER_ID);

        return userEntity;
    }

    public static UserEntity createUnauthorizedUserEntity() {
        UserEntity userEntity = UserEntity.builder()
            .username(UNAUTHORIZED_USERNAME)
            .password(ENCODED_PASSWORD)
            .role(USER_ROLE)
            .build();
        ReflectionTestUtils.setField(userEntity, "id", UNAUTHORIZED_USER_ID);

        return userEntity;
    }

    public static PostEntity createPostEntity(UserEntity userEntity) {
        return createPostEntity(CONTENT, userEntity);
    }

    public static PostEntity createPostEntity(String content, UserEntity userEntity) {
        PostEntity postEntity = PostEntity.builder()
            .content(content)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(postEntity, "id", POST_ID);

        return postEntity;
    }

    public static PostEntity createPostEntity(Long postId, String content, UserEntity userEntity) {
        PostEntity postEntity = PostEntity.builder()
            .content(content)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(postEntity, "id", postId);

        return postEntity;
    }

    public static PostEntity createPostEntity(
        Long postId,
        String content,
        Instant createdAt,
        UserEntity userEntity
    ) {
        PostEntity postEntity = PostEntity.builder()
            .content(content)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(postEntity, "id", postId);
        ReflectionTestUtils.setField(postEntity, "createdAt", createdAt);

        return postEntity;
    }

    public static CommentEntity createCommentEntity(PostEntity postEntity, UserEntity userEntity) {
        return createCommentEntity(CONTENT, postEntity, userEntity);
    }

    public static CommentEntity createCommentEntity(
        String content,
        PostEntity postEntity,
        UserEntity userEntity
    ) {
        CommentEntity commentEntity = CommentEntity.builder()
            .content(content)
            .post(postEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(commentEntity, "id", COMMENT_ID);

        return commentEntity;
    }

    public static CommentEntity createCommentEntity(
        Long commentId,
        String content,
        PostEntity postEntity,
        UserEntity userEntity
    ) {
        CommentEntity commentEntity = CommentEntity.builder()
            .content(content)
            .post(postEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(commentEntity, "id", commentId);

        return commentEntity;
    }

    public static ReplyEntity createReplyEntity(
        CommentEntity commentEntity,
        UserEntity userEntity
    ) {
        return createReplyEntity(CONTENT, commentEntity, userEntity);
    }

    public static ReplyEntity createReplyEntity(
        String content,
        CommentEntity commentEntity,
        UserEntity userEntity
    ) {
        ReplyEntity replyEntity = ReplyEntity.builder()
            .content(content)
            .comment(commentEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(replyEntity, "id", REPLY_ID);

        return replyEntity;
    }

    public static ReplyEntity createReplyEntity(
        Long replyId,
        String content,
        CommentEntity commentEntity,
        UserEntity userEntity
    ) {
        ReplyEntity replyEntity = ReplyEntity.builder()
            .content(content)
            .comment(commentEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(replyEntity, "id", replyId);

        return replyEntity;
    }
}
