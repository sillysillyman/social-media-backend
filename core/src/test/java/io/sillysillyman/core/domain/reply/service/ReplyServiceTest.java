package io.sillysillyman.core.domain.reply.service;

import static io.sillysillyman.core.common.constants.TestConstants.ANOTHER_COMMENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.ANOTHER_REPLY_ID;
import static io.sillysillyman.core.common.constants.TestConstants.COMMENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.DEFAULT_PAGE_SIZE;
import static io.sillysillyman.core.common.constants.TestConstants.FIRST_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.NON_EXISTENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.REPLY_ID;
import static io.sillysillyman.core.common.constants.TestConstants.SECOND_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.UPDATED_CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ID;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createCommentEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createPostEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createReplyEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUnauthorizedUserEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUserEntity;
import static io.sillysillyman.core.common.utils.TestUtils.assertPageProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.ForbiddenAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.comment.service.CommentService;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.reply.Reply;
import io.sillysillyman.core.domain.reply.ReplyEntity;
import io.sillysillyman.core.domain.reply.command.UpsertReplyCommand;
import io.sillysillyman.core.domain.reply.exception.ReplyErrorCode;
import io.sillysillyman.core.domain.reply.exception.detail.ReplyNotBelongToCommentException;
import io.sillysillyman.core.domain.reply.exception.detail.ReplyNotFoundException;
import io.sillysillyman.core.domain.reply.repository.ReplyRepository;
import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class ReplyServiceTest {

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ReplyService replyService;

    private User user;
    private Comment comment;
    private UserEntity userEntity;
    private PostEntity postEntity;
    private CommentEntity commentEntity;
    private ReplyEntity replyEntity;

    @BeforeEach
    void setUp() {
        userEntity = createUserEntity();
        postEntity = createPostEntity(userEntity);
        commentEntity = createCommentEntity(postEntity, userEntity);
        replyEntity = createReplyEntity(commentEntity, userEntity);

        user = User.from(userEntity);
        comment = Comment.from(commentEntity);
    }

    @DisplayName("답글 생성")
    @Nested
    class CreateComment {

        @DisplayName("답글 생성 성공")
        @Test
        void given_ValidCommand_when_CreateReply_then_ReturnSavedReply() {
            // given
            UpsertReplyCommand command = () -> CONTENT;

            ReplyEntity savedReplyEntity = createReplyEntity(commentEntity, userEntity);

            given(commentService.getById(COMMENT_ID)).willReturn(comment);
            given(replyRepository.save(any(ReplyEntity.class))).willReturn(savedReplyEntity);

            // when
            Reply reply = replyService.createReply(COMMENT_ID, command, user);

            // then
            assertThat(reply.getContent()).isEqualTo(CONTENT);
            assertThat(reply.getComment().getId()).isEqualTo(COMMENT_ID);
            assertThat(reply.getUser().getId()).isEqualTo(USER_ID);

            then(replyRepository).should().save(any(ReplyEntity.class));
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("답글 목록 조회")
    @Nested
    class GetComments {

        private static final String OLDER_REPLY_CONTENT = "older reply content";
        private static final String NEWER_REPLY_CONTENT = "newer reply content";
        private static final String SECOND_PAGE_OLDER_REPLY_CONTENT = "second page older reply content";
        private static final String SECOND_PAGE_NEWER_REPLY_CONTENT = "second page newer reply content";

        @DisplayName("댓글의 답글 목록 페이지 조회")
        @Test
        void given_CommentWithReplies_when_GetReplies_then_ReturnPageOfReplies() {
            // given
            Pageable pageable = PageRequest.of(FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            List<ReplyEntity> replyEntities = List.of(
                createReplyEntity(REPLY_ID, OLDER_REPLY_CONTENT, commentEntity, userEntity),
                createReplyEntity(ANOTHER_REPLY_ID, NEWER_REPLY_CONTENT, commentEntity, userEntity)
            );

            Page<ReplyEntity> replyEntityPage = new PageImpl<>(
                replyEntities,
                pageable,
                replyEntities.size()
            );

            given(replyRepository.findByCommentId(COMMENT_ID, pageable))
                .willReturn(replyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(COMMENT_ID, pageable);

            // then
            assertPageProperties(replyPage, 2, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 2,
                content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(OLDER_REPLY_CONTENT);
                    assertThat(content.get(1).getContent()).isEqualTo(NEWER_REPLY_CONTENT);
                    content.forEach(reply -> {
                        assertThat(reply.getComment().getId()).isEqualTo(COMMENT_ID);
                        assertThat(reply.getUser().getId()).isEqualTo(USER_ID);
                    });
                }
            );

            then(replyRepository).should().findByCommentId(COMMENT_ID, pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("답글 없는 댓글의 답글 목록 조회")
        @Test
        void given_CommentWithNoReplies_when_GetReplies_then_ReturnEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            Page<ReplyEntity> emptyReplyEntityPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
            );

            given(replyRepository.findByCommentId(COMMENT_ID, pageable))
                .willReturn(emptyReplyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(COMMENT_ID, pageable);

            // then
            assertPageProperties(replyPage, 0, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 0);

            then(replyRepository).should().findByCommentId(COMMENT_ID, pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("댓글의 두 번째 페이지 답글 목록 조회")
        @Test
        void given_MultipleReplies_when_GetSecondPage_then_ReturnSecondPageOfReplies() {
            // given
            Pageable pageable = PageRequest.of(SECOND_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            List<ReplyEntity> replyEntities = List.of(
                createReplyEntity(
                    REPLY_ID,
                    SECOND_PAGE_OLDER_REPLY_CONTENT,
                    commentEntity,
                    userEntity
                ),
                createReplyEntity(
                    ANOTHER_REPLY_ID,
                    SECOND_PAGE_NEWER_REPLY_CONTENT,
                    commentEntity,
                    userEntity
                )
            );

            Page<ReplyEntity> replyEntityPage = new PageImpl<>(replyEntities, pageable, 12);

            given(replyRepository.findByCommentId(COMMENT_ID, pageable))
                .willReturn(replyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(COMMENT_ID, pageable);

            // then
            assertPageProperties(replyPage, 2, SECOND_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 12,
                content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(
                        SECOND_PAGE_OLDER_REPLY_CONTENT
                    );
                    assertThat(content.get(1).getContent()).isEqualTo(
                        SECOND_PAGE_NEWER_REPLY_CONTENT
                    );
                    content.forEach(reply -> {
                        assertThat(reply.getComment().getId()).isEqualTo(COMMENT_ID);
                        assertThat(reply.getUser().getId()).isEqualTo(USER_ID);
                    });
                }
            );

            then(replyRepository).should().findByCommentId(COMMENT_ID, pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("답글 수정")
    @Nested
    class UpdateComment {

        @DisplayName("답글 수정 성공")
        @Test
        void given_ValidCommand_when_UpdateReply_then_ReplyUpdatedSuccessfully() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            ReplyEntity updatedReplyEntity = createReplyEntity(
                UPDATED_CONTENT,
                commentEntity,
                userEntity
            );

            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));
            given(replyRepository.save(any(ReplyEntity.class))).willReturn(updatedReplyEntity);

            // when
            replyService.updateReply(COMMENT_ID, REPLY_ID, command, user);
            Reply updatedReply = Reply.from(updatedReplyEntity);

            // then
            assertThat(updatedReply.getContent()).isEqualTo(UPDATED_CONTENT);

            then(replyRepository).should().save(any(ReplyEntity.class));
            then(replyRepository).should().findById(REPLY_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("존재하지 않는 답글 수정 실패")
        @Test
        void given_NonExistentReply_when_UpdateReply_then_ThrowReplyNotFoundException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            given(replyRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> replyService.updateReply(
                COMMENT_ID,
                NON_EXISTENT_ID,
                command,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotFoundException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_FOUND.getMessage());

            then(replyRepository).should().findById(NON_EXISTENT_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("권한이 없는 사용자의 답글 수정 실패")
        @Test
        void given_UnauthorizedUser_when_UpdateReply_then_ThrowForbiddenAccessException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () -> replyService.updateReply(
                COMMENT_ID,
                REPLY_ID,
                command,
                unauthorizedUser
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(replyRepository).should().findById(REPLY_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("다른 게시글의 답글 수정 실패")
        @Test
        void given_ReplyFromDifferentComment_when_UpdateReply_then_ThrowReplyNotBelongToCommentException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () -> replyService.updateReply(
                ANOTHER_COMMENT_ID,
                REPLY_ID,
                command,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotBelongToCommentException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_BELONG_TO_COMMENT.getMessage());

            then(replyRepository).should().findById(REPLY_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("답글 삭제")
    @Nested
    class DeleteComment {

        @DisplayName("답글 삭제 성공")
        @Test
        void given_ExistingReply_when_DeleteReply_then_ReplyDeletedSuccessfully() {
            // given
            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));

            // when
            replyService.deleteReply(COMMENT_ID, REPLY_ID, user);

            // then
            then(replyRepository).should().findById(REPLY_ID);
            then(replyRepository).should().delete(any(ReplyEntity.class));
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("존재하지 않는 답글 삭제 실패")
        @Test
        void given_NonExistentReply_when_DeleteReply_then_ThrowReplyNotFoundException() {
            // given
            given(replyRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> replyService.deleteReply(
                COMMENT_ID,
                NON_EXISTENT_ID,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotFoundException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_FOUND.getMessage());

            then(replyRepository).should().findById(NON_EXISTENT_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("권한이 없는 사용자의 답글 삭제 실패")
        @Test
        void given_UnauthorizedUser_when_DeleteReply_then_ThrowForbiddenAccessException() {
            // given
            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () -> replyService.deleteReply(
                COMMENT_ID,
                REPLY_ID,
                unauthorizedUser
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(replyRepository).should().findById(COMMENT_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("다른 댓글의 답글 삭제 실패")
        @Test
        void given_ReplyFromDifferentComment_when_DeleteReply_then_ThrowReplyNotBelongToCommentException() {
            // given
            given(replyRepository.findById(REPLY_ID)).willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () -> replyService.deleteReply(
                ANOTHER_COMMENT_ID,
                REPLY_ID,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotBelongToCommentException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_BELONG_TO_COMMENT.getMessage());

            then(replyRepository).should().findById(REPLY_ID);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }
}
