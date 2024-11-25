package io.sillysillyman.core.domain.reply.service;

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
import io.sillysillyman.core.domain.post.Post;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ReplyServiceTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_CONTENT = "Test content";
    private static final Long DEFAULT_ID = 1L;
    private static final Long ANOTHER_ID = 2L;
    private static final Long NON_EXISTENT_ID = 999L;

    @Mock
    private ReplyRepository replyRepository;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private ReplyService replyService;

    private User user;
    private Post post;
    private Comment comment;
    private Reply reply;
    private UserEntity userEntity;
    private PostEntity postEntity;
    private CommentEntity commentEntity;
    private ReplyEntity replyEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
            .username(TEST_USERNAME)
            .build();
        ReflectionTestUtils.setField(userEntity, "id", DEFAULT_ID);

        postEntity = PostEntity.builder()
            .content(TEST_CONTENT)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(postEntity, "id", DEFAULT_ID);

        commentEntity = CommentEntity.builder()
            .content(TEST_CONTENT)
            .post(postEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(commentEntity, "id", DEFAULT_ID);

        replyEntity = ReplyEntity.builder()
            .content(TEST_CONTENT)
            .comment(commentEntity)
            .user(userEntity)
            .build();
        ReflectionTestUtils.setField(replyEntity, "id", DEFAULT_ID);

        user = User.from(userEntity);
        post = Post.from(postEntity);
        comment = Comment.from(commentEntity);
        reply = Reply.from(replyEntity);
    }

    private void verifyRepositoryFindById(Long commentId) {
        then(replyRepository).should().findById(commentId);
        then(replyRepository).shouldHaveNoMoreInteractions();
    }

    private User createUnauthorizedUser() {
        UserEntity unauthorizedUserEntity = UserEntity.builder()
            .username("unauthorizedUser")
            .build();
        ReflectionTestUtils.setField(unauthorizedUserEntity, "id", ANOTHER_ID);
        return User.from(unauthorizedUserEntity);
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        private static final String NEW_REPLY_CONTENT = "New reply content";

        @Test
        @DisplayName("유효한 요청으로 답글 생성")
        void given_ValidReplyRequest_when_CreateReply_then_ReplySavedSuccessfully() {
            // given
            UpsertReplyCommand command = () -> NEW_REPLY_CONTENT;

            ReplyEntity savedReplyEntity = ReplyEntity.builder()
                .content(NEW_REPLY_CONTENT)
                .comment(commentEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(savedReplyEntity, "id", DEFAULT_ID);

            given(commentService.getById(commentEntity.getId())).willReturn(comment);
            given(replyRepository.save(any(ReplyEntity.class))).willReturn(savedReplyEntity);

            // when
            Reply reply = replyService.createReply(commentEntity.getId(), command, user);

            // then
            assertThat(reply.getContent()).isEqualTo(NEW_REPLY_CONTENT);
            assertThat(reply.getComment().getId()).isEqualTo(comment.getId());
            assertThat(reply.getUser().getId()).isEqualTo(user.getId());

            then(replyRepository).should().save(any(ReplyEntity.class));
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("답글 목록 조회")
    class GetComments {

        private static final String FIRST_REPLY = "1st reply";
        private static final String SECOND_REPLY = "2nd reply";
        private static final String SIXTH_REPLY = "6th reply";
        private static final String SEVENTH_REPLY = "7th reply";
        private static final int DEFAULT_PAGE_SIZE = 10;

        @Test
        @DisplayName("댓글의 답글 목록을 페이지네이션과 함께 조회")
        void given_CommentWithReplies_when_GetReplies_then_ReturnPaginatedReplies() {
            // given
            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
            List<ReplyEntity> replyEntities = List.of(
                createReplyEntityWithId(FIRST_REPLY, DEFAULT_ID),
                createReplyEntityWithId(SECOND_REPLY, ANOTHER_ID)
            );
            Page<ReplyEntity> replyEntityPage = new PageImpl<>(
                replyEntities,
                pageable,
                replyEntities.size()
            );

            given(replyRepository.findByCommentId(commentEntity.getId(), pageable))
                .willReturn(replyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(comment.getId(), pageable);

            // then
            verifyPage(replyPage, 2, 0, DEFAULT_PAGE_SIZE, 2);

            assertThat(replyPage.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(FIRST_REPLY);
                    assertThat(content.get(1).getContent()).isEqualTo(SECOND_REPLY);
                });

            verifyRepositoryFindByCommentId(pageable);
        }

        @Test
        @DisplayName("답글 없는 댓글의 답글 목록 조회")
        void given_CommentWithNoReplies_when_GetReplies_then_ReturnEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
            Page<ReplyEntity> emptyReplyEntityPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
            );

            given(replyRepository.findByCommentId(commentEntity.getId(), pageable))
                .willReturn(emptyReplyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(commentEntity.getId(), pageable);

            // then
            verifyPage(replyPage, 0, 0, DEFAULT_PAGE_SIZE, 0);

            then(replyRepository).should().findByCommentId(comment.getId(), pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("두 번째 페이지의 답글 목록 조회")
        void given_MultipleReplies_when_GetSecondPage_then_ReturnCorrectPageOfReplies() {
            // given
            Pageable pageable = PageRequest.of(1, 5);
            List<ReplyEntity> replyEntities = List.of(
                createReplyEntityWithId(SIXTH_REPLY, DEFAULT_ID),
                createReplyEntityWithId(SEVENTH_REPLY, ANOTHER_ID)
            );

            replyEntities.forEach(replyEntity ->
                ReflectionTestUtils.setField(
                    replyEntity,
                    "id",
                    replyEntities.indexOf(replyEntity) + 6L
                )
            );

            Page<ReplyEntity> replyEntityPage = new PageImpl<>(replyEntities, pageable, 7);

            given(replyRepository.findByCommentId(commentEntity.getId(), pageable))
                .willReturn(replyEntityPage);

            // when
            Page<Reply> replyPage = replyService.getReplies(commentEntity.getId(), pageable);

            // then
            verifyPage(replyPage, 2, 1, 5, 7);

            assertThat(replyPage.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(SIXTH_REPLY);
                    assertThat(content.get(1).getContent()).isEqualTo(SEVENTH_REPLY);
                });

            then(replyRepository).should().findByCommentId(comment.getId(), pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyRepositoryFindByCommentId(Pageable pageable) {
            then(replyRepository).should().findByCommentId(comment.getId(), pageable);
            then(replyRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyPage(
            Page<?> page,
            int expectedSize,
            int expectedNumber,
            int expectedPageSize,
            int expectedTotalElements
        ) {
            assertThat(page.getContent()).hasSize(expectedSize);
            assertThat(page.getNumber()).isEqualTo(expectedNumber);
            assertThat(page.getSize()).isEqualTo(expectedPageSize);
            assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
        }

        private ReplyEntity createReplyEntityWithId(String content, Long id) {
            ReplyEntity newReplyEntity = ReplyEntity.builder()
                .content(content)
                .comment(commentEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(newReplyEntity, "id", id);
            return newReplyEntity;
        }
    }

    @Nested
    @DisplayName("답글 수정")
    class UpdateComment {

        private static final String UPDATED_CONTENT = "Updated content";

        @Test
        @DisplayName("정상적인 답글 수정")
        void given_ValidUpdateRequest_when_UpdateReply_then_ReplyUpdatedSuccessfully() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;
            ReplyEntity updatedReplyEntity = ReplyEntity.builder()
                .content(UPDATED_CONTENT)
                .comment(commentEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(updatedReplyEntity, "id", DEFAULT_ID);

            given(replyRepository.findById(replyEntity.getId()))
                .willReturn(Optional.of(replyEntity));
            given(replyRepository.save(any(ReplyEntity.class)))
                .willReturn(updatedReplyEntity);

            // when
            replyService.updateReply(comment.getId(), reply.getId(), command, user);
            Reply updatedReply = Reply.from(updatedReplyEntity);

            // then
            assertThat(updatedReply.getContent()).isEqualTo(UPDATED_CONTENT);
            then(replyRepository).should().save(any(ReplyEntity.class));
            verifyRepositoryFindById(reply.getId());
        }

        @Test
        @DisplayName("존재하지 않는 답글 수정 시도")
        void given_NonExistentReply_when_UpdateReply_then_ThrowReplyNotFoundException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            given(replyRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                replyService.updateReply(commentEntity.getId(), NON_EXISTENT_ID, command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotFoundException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 답글 수정 시도")
        void given_UnauthorizedUser_when_UpdateReply_then_ThrowForbiddenAccessException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;
            User unauthorizedUser = createUnauthorizedUser();

            given(replyRepository.findById(replyEntity.getId()))
                .willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () ->
                replyService.updateReply(
                    commentEntity.getId(),
                    replyEntity.getId(),
                    command,
                    unauthorizedUser
                );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            verifyRepositoryFindById(reply.getId());
        }

        @Test
        @DisplayName("다른 게시글의 답글 수정 시도")
        void given_ReplyFromDifferentComment_when_UpdateReply_then_ThrowReplyNotBelongToCommentException() {
            // given
            UpsertReplyCommand command = () -> UPDATED_CONTENT;

            CommentEntity anotherCommentEntity = CommentEntity.builder()
                .content("Another post content")
                .post(postEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(anotherCommentEntity, "id", ANOTHER_ID);
            Comment anotherComment = Comment.from(anotherCommentEntity);

            given(replyRepository.findById(reply.getId()))
                .willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () ->
                replyService.updateReply(anotherComment.getId(), reply.getId(), command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotBelongToCommentException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_BELONG_TO_COMMENT.getMessage());

            verifyRepositoryFindById(reply.getId());
        }
    }

    @Nested
    @DisplayName("답글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("정상적인 답글 삭제")
        void given_ValidReply_when_DeleteReply_then_ReplyDeletedSuccessfully() {
            // given
            given(replyRepository.findById(replyEntity.getId()))
                .willReturn(Optional.of(replyEntity));

            // when
            replyService.deleteReply(comment.getId(), reply.getId(), user);

            // then
            verifyRepositoryDelete(reply.getId());
        }

        @Test
        @DisplayName("존재하지 않는 답글 삭제 시도")
        void given_NonExistentReply_when_DeleteReply_then_ThrowReplyNotFoundException() {
            // given
            given(replyRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                replyService.deleteReply(comment.getId(), NON_EXISTENT_ID, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotFoundException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 답글 삭제 시도")
        void given_UnauthorizedUser_when_DeleteReply_then_ThrowForbiddenAccessException() {
            // given
            User unauthorizedUser = createUnauthorizedUser();
            given(replyRepository.findById(replyEntity.getId()))
                .willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () ->
                replyService.deleteReply(comment.getId(), reply.getId(), unauthorizedUser);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            verifyRepositoryFindById(reply.getId());
        }

        @Test
        @DisplayName("다른 댓글의 답글 삭제 시도")
        void given_ReplyFromDifferentComment_when_DeleteReply_then_ThrowReplyNotBelongToCommentException() {
            // given
            CommentEntity anotherCommentEntity = CommentEntity.builder()
                .content(TEST_CONTENT)
                .post(postEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(anotherCommentEntity, "id", ANOTHER_ID);
            Comment anotherComment = Comment.from(anotherCommentEntity);

            given(replyRepository.findById(reply.getId())).willReturn(Optional.of(replyEntity));

            // when
            ThrowingCallable when = () ->
                replyService.deleteReply(anotherComment.getId(), reply.getId(), user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ReplyNotBelongToCommentException.class)
                .hasMessage(ReplyErrorCode.REPLY_NOT_BELONG_TO_COMMENT.getMessage());

            verifyRepositoryFindById(reply.getId());
        }

        private void verifyRepositoryDelete(Long commentId) {
            then(replyRepository).should().findById(commentId);
            then(replyRepository).should().delete(any(ReplyEntity.class));
            then(replyRepository).shouldHaveNoMoreInteractions();
        }
    }
}
