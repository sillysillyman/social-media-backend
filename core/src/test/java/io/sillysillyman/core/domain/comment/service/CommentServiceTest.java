package io.sillysillyman.core.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.core.domain.comment.Comment;
import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotBelongToPostException;
import io.sillysillyman.core.domain.comment.exception.detail.CommentNotFoundException;
import io.sillysillyman.core.domain.comment.repository.CommentRepository;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.service.PostService;
import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CommentResponse;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CreateCommentRequest;
import io.sillysillyman.socialmediabackend.domain.comment.dto.UpdateCommentRequest;
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

    @BeforeEach
    void setUp() {
        user = User.builder()
            .username(TEST_USERNAME)
            .build();
        ReflectionTestUtils.setField(user, "id", DEFAULT_ID);

        post = Post.builder()
            .content(TEST_CONTENT)
            .user(user)
            .build();
        ReflectionTestUtils.setField(post, "id", DEFAULT_ID);

        comment = Comment.builder()
            .content(TEST_CONTENT)
            .post(post)
            .user(user)
            .build();
        ReflectionTestUtils.setField(comment, "id", DEFAULT_ID);
    }

    private void verifyRepositoryFindById(Long commentId) {
        then(commentRepository).should().findById(commentId);
        then(commentRepository).shouldHaveNoMoreInteractions();
    }

    private User createUnauthorizedUser() {
        User unauthorizedUser = User.builder()
            .username("unauthorizedUser")
            .build();
        ReflectionTestUtils.setField(unauthorizedUser, "id", ANOTHER_ID);
        return unauthorizedUser;
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreateComment {

        private static final String NEW_COMMENT_CONTENT = "New comment content";

        @Test
        @DisplayName("유효한 요청으로 댓글 생성")
        void createsCommentWithValidRequest() {
            // given
            CreateCommentRequest request = new CreateCommentRequest();
            ReflectionTestUtils.setField(request, "content", NEW_COMMENT_CONTENT);

            Comment savedComment = Comment.builder()
                .content(NEW_COMMENT_CONTENT)
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(savedComment, "id", DEFAULT_ID);

            given(postService.getById(post.getId())).willReturn(post);
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(post.getId(), request, user);

            // then
            assertThat(response.content()).isEqualTo(NEW_COMMENT_CONTENT);
            assertThat(response.postResponse().postId()).isEqualTo(post.getId());
            assertThat(response.userResponse().userId()).isEqualTo(user.getId());

            then(commentRepository).should().save(any(Comment.class));
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
            List<Comment> comments = createCommentsList(FIRST_COMMENT, SECOND_COMMENT);
            Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

            given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(commentPage);

            // when
            Page<CommentResponse> response = commentService.getComments(post.getId(), pageable);

            // then
            verifyPageResponse(response, 2, 0, DEFAULT_PAGE_SIZE);
            verifyCommentContents(response);
            verifyRepositoryFindByPostId(pageable);
        }

        @Test
        @DisplayName("댓글이 없는 게시글의 댓글 목록 조회")
        void getsEmptyCommentsWhenNoComments() {
            // given
            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(emptyPage);

            // when
            Page<CommentResponse> response = commentService.getComments(post.getId(), pageable);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getNumber()).isZero();
            assertThat(response.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
            assertThat(response.getTotalElements()).isZero();

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("두 번째 페이지의 댓글 목록 조회")
        void getsSecondPageOfComments() {
            // given
            Pageable pageable = PageRequest.of(1, 5);
            List<Comment> comments = createCommentsList(SIXTH_COMMENT, SEVENTH_COMMENT);

            comments.forEach(comment ->
                ReflectionTestUtils.setField(comment, "id", comments.indexOf(comment) + 6L)
            );

            Page<Comment> commentPage = new PageImpl<>(comments, pageable, 7);

            given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(commentPage);

            // when
            Page<CommentResponse> response = commentService.getComments(post.getId(), pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getNumber()).isEqualTo(1);
            assertThat(response.getSize()).isEqualTo(5);
            assertThat(response.getTotalElements()).isEqualTo(7);

            assertThat(response.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).content()).isEqualTo(SIXTH_COMMENT);
                    assertThat(content.get(1).content()).isEqualTo(SEVENTH_COMMENT);
                });

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyRepositoryFindByPostId(Pageable pageable) {
            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyPageResponse(
            Page<?> page,
            int expectedSize,
            int expectedNumber,
            int expectedPageSize
        ) {
            assertThat(page.getContent()).hasSize(expectedSize);
            assertThat(page.getNumber()).isEqualTo(expectedNumber);
            assertThat(page.getSize()).isEqualTo(expectedPageSize);
        }

        private void verifyCommentContents(Page<CommentResponse> response) {
            assertThat(response.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).content()).isEqualTo(FIRST_COMMENT);
                    assertThat(content.get(1).content()).isEqualTo(SECOND_COMMENT);
                    content.forEach(this::verifyCommentResponse);
                });
        }

        private void verifyCommentResponse(CommentResponse response) {
            assertThat(response.postResponse().postId()).isEqualTo(post.getId());
            assertThat(response.userResponse().userId()).isEqualTo(user.getId());
        }

        private Comment createCommentWithId(String content, Long id) {
            Comment newComment = Comment.builder()
                .content(content)
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(newComment, "id", id);
            return newComment;
        }

        private List<Comment> createCommentsList(String content1, String content2) {
            return List.of(
                createCommentWithId(content1, DEFAULT_ID),
                createCommentWithId(content2, ANOTHER_ID)
            );
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
            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            commentService.updateComment(post.getId(), comment.getId(), request, user);

            // then
            assertThat(comment.getContent()).isEqualTo(UPDATED_CONTENT);
            verifyRepositoryFindById(comment.getId());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시도")
        void throwsExceptionWhenCommentNotFound() {
            // given

            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);

            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(post.getId(), NON_EXISTENT_ID, request, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            then(commentRepository).should().findById(NON_EXISTENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("권한이 없는 사용자의 댓글 수정 시도")
        void throwsExceptionWhenUnauthorizedUser() {
            // given
            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
            User unauthorizedUser = createUnauthorizedUser();

            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(
                    post.getId(),
                    comment.getId(),
                    request,
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
            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);

            Post anotherPost = Post.builder()
                .content("Another post content")
                .user(user)
                .build();
            ReflectionTestUtils.setField(anotherPost, "id", ANOTHER_ID);

            Comment foundComment = Comment.builder()
                .content("Original content")
                .post(anotherPost)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", DEFAULT_ID);

            given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(foundComment));

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(post.getId(), comment.getId(), request, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            then(commentRepository).should().findById(comment.getId());
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        private UpdateCommentRequest createUpdateRequest(String content) {
            UpdateCommentRequest request = new UpdateCommentRequest();
            ReflectionTestUtils.setField(request, "content", content);
            return request;
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("정상적인 댓글 삭제")
        void deletesCommentSuccessfully() {
            // given
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

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
            given(commentRepository.findById(comment.getId())).willReturn(Optional.of(comment));

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
            Post anotherPost = Post.builder()
                .content(TEST_CONTENT)
                .user(user)
                .build();
            ReflectionTestUtils.setField(anotherPost, "id", ANOTHER_ID);

            Comment commentInAnotherPost = Comment.builder()
                .content(TEST_CONTENT)
                .post(anotherPost)
                .user(user)
                .build();
            ReflectionTestUtils.setField(commentInAnotherPost, "id", comment.getId());

            given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(commentInAnotherPost));

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), comment.getId(), user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            verifyRepositoryFindById(comment.getId());
        }

        private void verifyRepositoryDelete(Long commentId) {
            then(commentRepository).should().findById(commentId);
            then(commentRepository).should().delete(comment);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }
}
