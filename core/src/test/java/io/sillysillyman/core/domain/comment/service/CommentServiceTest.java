package io.sillysillyman.core.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.comment.command.CreateCommentCommand;
import io.sillysillyman.core.domain.comment.command.UpdateCommentCommand;
import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotBelongToPostException;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotFoundException;
import io.sillysillyman.core.domain.comment.repository.CommentRepository;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.post.service.PostService;
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
public class CommentServiceTest {

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_CONTENT = "Test content";
    private static final Long DEFAULT_ID = 1L;
    private static final Long ANOTHER_ID = 2L;
    private static final Long NON_EXISTENT_ID = 999L;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;
    private Comment comment;
    private UserEntity userEntity;
    private PostEntity postEntity;
    private CommentEntity commentEntity;

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

        user = User.from(userEntity);
        post = Post.from(postEntity);
        comment = Comment.from(commentEntity);
    }

    private void verifyRepositoryFindById(Long commentId) {
        then(commentRepository).should().findById(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
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

        private static final String NEW_COMMENT_CONTENT = "New comment content";

        @Test
        @DisplayName("유효한 요청으로 댓글 생성")
        void createsCommentWithValidRequest() {
            // given
            CreateCommentCommand command = () -> NEW_COMMENT_CONTENT;

            CommentEntity savedCommentEntity = CommentEntity.builder()
                .content(NEW_COMMENT_CONTENT)
                .post(postEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(savedCommentEntity, "id", DEFAULT_ID);

            given(postService.getById(postEntity.getId())).willReturn(post);
            given(commentRepository.save(any(CommentEntity.class))).willReturn(savedCommentEntity);

            // when
            Comment comment = commentService.createComment(postEntity.getId(), command, user);

            // then
            assertThat(comment.getContent()).isEqualTo(NEW_COMMENT_CONTENT);
            assertThat(comment.getPost().getId()).isEqualTo(post.getId());
            assertThat(comment.getUser().getId()).isEqualTo(user.getId());

            then(commentRepository).should().save(any(CommentEntity.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        private static final String FIRST_COMMENT = "1st comment";
        private static final String SECOND_COMMENT = "2nd comment";
        private static final String SIXTH_COMMENT = "6th comment";
        private static final String SEVENTH_COMMENT = "7th comment";
        private static final int DEFAULT_PAGE_SIZE = 10;

        @Test
        @DisplayName("게시글의 댓글 목록을 페이지네이션과 함께 조회")
        void getsCommentsWithPagination() {
            // given
            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
            List<CommentEntity> commentEntities = List.of(
                createCommentEntityWithId(FIRST_COMMENT, DEFAULT_ID),
                createCommentEntityWithId(SECOND_COMMENT, ANOTHER_ID)
            );
            Page<CommentEntity> commentEntityPage = new PageImpl<>(
                commentEntities,
                pageable,
                commentEntities.size()
            );

            given(commentRepository.findByPostId(post.getId(), pageable))
                .willReturn(commentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(post.getId(), pageable);

            // then
            verifyPage(commentPage, 2, 0, DEFAULT_PAGE_SIZE);
            verifyCommentContents(commentPage);
            verifyRepositoryFindByPostId(pageable);
        }

        @Test
        @DisplayName("댓글이 없는 게시글의 댓글 목록 조회")
        void getsEmptyCommentsWhenNoComments() {
            // given
            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
            Page<CommentEntity> emptyCommentEntityPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
            );

            given(commentRepository.findByPostId(postEntity.getId(), pageable))
                .willReturn(emptyCommentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(postEntity.getId(), pageable);

            // then
            assertThat(commentPage.getContent()).isEmpty();
            assertThat(commentPage.getNumber()).isZero();
            assertThat(commentPage.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
            assertThat(commentPage.getTotalElements()).isZero();

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("두 번째 페이지의 댓글 목록 조회")
        void getsSecondPageOfComments() {
            // given
            Pageable pageable = PageRequest.of(1, 5);
            List<CommentEntity> commentEntities = List.of(
                createCommentEntityWithId(SIXTH_COMMENT, DEFAULT_ID),
                createCommentEntityWithId(SEVENTH_COMMENT, ANOTHER_ID)
            );

            commentEntities.forEach(commentEntity ->
                ReflectionTestUtils.setField(
                    commentEntity,
                    "id",
                    commentEntities.indexOf(commentEntity) + 6L
                )
            );

            Page<CommentEntity> commentEntityPage = new PageImpl<>(commentEntities, pageable, 7);

            given(commentRepository.findByPostId(postEntity.getId(), pageable))
                .willReturn(commentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(postEntity.getId(), pageable);

            // then
            assertThat(commentPage.getContent()).hasSize(2);
            assertThat(commentPage.getNumber()).isEqualTo(1);
            assertThat(commentPage.getSize()).isEqualTo(5);
            assertThat(commentPage.getTotalElements()).isEqualTo(7);

            assertThat(commentPage.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(SIXTH_COMMENT);
                    assertThat(content.get(1).getContent()).isEqualTo(SEVENTH_COMMENT);
                });

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyRepositoryFindByPostId(Pageable pageable) {
            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyPage(
            Page<?> page,
            int expectedSize,
            int expectedNumber,
            int expectedPageSize
        ) {
            assertThat(page.getContent()).hasSize(expectedSize);
            assertThat(page.getNumber()).isEqualTo(expectedNumber);
            assertThat(page.getSize()).isEqualTo(expectedPageSize);
        }

        private void verifyCommentContents(Page<Comment> response) {
            assertThat(response.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(FIRST_COMMENT);
                    assertThat(content.get(1).getContent()).isEqualTo(SECOND_COMMENT);
                    content.forEach(this::verifyComment);
                });
        }

        private void verifyComment(Comment comment) {
            assertThat(comment.getPost().getId()).isEqualTo(post.getId());
            assertThat(comment.getUser().getId()).isEqualTo(user.getId());
        }

        private CommentEntity createCommentEntityWithId(String content, Long id) {
            CommentEntity newCommentEntity = CommentEntity.builder()
                .content(content)
                .post(postEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(newCommentEntity, "id", id);
            return newCommentEntity;
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        private static final String UPDATED_CONTENT = "Updated content";

        @Test
        @DisplayName("정상적인 댓글 수정")
        void updatesCommentSuccessfully() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;
            CommentEntity updatedCommentEntity = CommentEntity.builder()
                .content(UPDATED_CONTENT)
                .post(postEntity)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(updatedCommentEntity, "id", DEFAULT_ID);

            given(commentRepository.findById(commentEntity.getId()))
                .willReturn(Optional.of(commentEntity));
            given(commentRepository.save(any(CommentEntity.class)))
                .willReturn(updatedCommentEntity);

            // when
            commentService.updateComment(post.getId(), comment.getId(), command, user);
            Comment updatedComment = Comment.from(updatedCommentEntity);

            // then
            assertThat(updatedComment.getContent()).isEqualTo(UPDATED_CONTENT);
            then(commentRepository).should().save(any(CommentEntity.class));
            verifyRepositoryFindById(comment.getId());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시도")
        void throwsExceptionWhenCommentNotFound() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(postEntity.getId(), NON_EXISTENT_ID, command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 댓글 수정 시도")
        void throwsExceptionWhenUnauthorizedUser() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;
            User unauthorizedUser = createUnauthorizedUser();

            given(commentRepository.findById(commentEntity.getId()))
                .willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(
                    postEntity.getId(),
                    commentEntity.getId(),
                    command,
                    unauthorizedUser
                );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());

            verifyRepositoryFindById(comment.getId());
        }

        @Test
        @DisplayName("다른 게시글의 댓글 수정 시도")
        void throwsExceptionWhenCommentNotBelongToPost() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            PostEntity anotherPostEntity = PostEntity.builder()
                .content("Another post content")
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(anotherPostEntity, "id", ANOTHER_ID);
            Post anotherPost = Post.from(anotherPostEntity);

            given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(anotherPost.getId(), comment.getId(), command, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            verifyRepositoryFindById(comment.getId());
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("정상적인 댓글 삭제")
        void deletesCommentSuccessfully() {
            // given
            given(commentRepository.findById(commentEntity.getId()))
                .willReturn(Optional.of(commentEntity));

            // when
            commentService.deleteComment(post.getId(), comment.getId(), user);

            // then
            verifyRepositoryDelete(comment.getId());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시도")
        void throwsExceptionWhenCommentNotFound() {
            // given
            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), NON_EXISTENT_ID, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 댓글 삭제 시도")
        void throwsExceptionWhenUnauthorizedUser() {
            // given
            User unauthorizedUser = createUnauthorizedUser();
            given(commentRepository.findById(commentEntity.getId()))
                .willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), comment.getId(), unauthorizedUser);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());

            verifyRepositoryFindById(comment.getId());
        }

        @Test
        @DisplayName("다른 게시글의 댓글 삭제 시도")
        void throwsExceptionWhenCommentNotBelongToPost() {
            // given
            PostEntity anotherPostEntity = PostEntity.builder()
                .content(TEST_CONTENT)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(anotherPostEntity, "id", ANOTHER_ID);
            Post anotherPost = Post.from(anotherPostEntity);

            given(commentRepository.findById(comment.getId())).willReturn(
                Optional.of(commentEntity));

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(anotherPost.getId(), comment.getId(), user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            verifyRepositoryFindById(comment.getId());
        }

        private void verifyRepositoryDelete(Long commentId) {
            then(commentRepository).should().findById(commentId);
            then(commentRepository).should().delete(any(CommentEntity.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }
}
