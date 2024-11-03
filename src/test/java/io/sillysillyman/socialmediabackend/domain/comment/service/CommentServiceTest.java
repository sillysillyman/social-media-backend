package io.sillysillyman.socialmediabackend.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.socialmediabackend.auth.exception.AuthErrorCode;
import io.sillysillyman.socialmediabackend.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.socialmediabackend.domain.comment.Comment;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CommentResponse;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CreateCommentRequest;
import io.sillysillyman.socialmediabackend.domain.comment.dto.UpdateCommentRequest;
import io.sillysillyman.socialmediabackend.domain.comment.exception.CommentErrorCode;
import io.sillysillyman.socialmediabackend.domain.comment.exception.detail.CommentNotBelongToPostException;
import io.sillysillyman.socialmediabackend.domain.comment.exception.detail.CommentNotFoundException;
import io.sillysillyman.socialmediabackend.domain.comment.repository.CommentRepository;
import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.post.service.PostService;
import io.sillysillyman.socialmediabackend.domain.user.User;
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
            .username("testUser")
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        post = Post.builder()
            .content("Post test content")
            .user(user)
            .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        comment = Comment.builder()
            .content("Comment test content")
            .post(post)
            .user(user)
            .build();
        ReflectionTestUtils.setField(comment, "id", 1L);
    }

    @Nested
    @DisplayName("댓글 생성")
    class CreatePost {

        @Test
        @DisplayName("유효한 요청으로 댓글 생성")
        void createsCommentWithValidRequest() {
            // given
            CreateCommentRequest request = new CreateCommentRequest();
            ReflectionTestUtils.setField(request, "content", "New comment content");

            Comment savedComment = Comment.builder()
                .content("New comment content")
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(savedComment, "id", 1L);

            given(postService.getById(post.getId())).willReturn(post);
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(post.getId(), request, user);

            // then
            assertThat(response)
                .satisfies(r -> {
                    assertThat(r.content()).isEqualTo("New comment content");
                    assertThat(r.postResponse().postId()).isEqualTo(post.getId());
                    assertThat(r.userResponse().userId()).isEqualTo(user.getId());
                });

            then(commentRepository).should().save(any(Comment.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("게시글의 댓글 목록을 페이지네이션과 함께 조회")
        void getsCommentsWithPagination() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Comment> comments = List.of(
                Comment.builder()
                    .content("첫 번째 댓글")
                    .post(post)
                    .user(user)
                    .build(),
                Comment.builder()
                    .content("두 번째 댓글")
                    .post(post)
                    .user(user)
                    .build()
            );
            comments.forEach(comment ->
                ReflectionTestUtils.setField(comment, "id", comments.indexOf(comment) + 1L)
            );

            Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

            given(commentRepository.findByPostId(post.getId(), pageable)).willReturn(commentPage);

            // when
            Page<CommentResponse> response = commentService.getComments(post.getId(), pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getNumber()).isZero();
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isEqualTo(2);

            assertThat(response.getContent())
                .satisfies(content -> {
                    assertThat(content.get(0).content()).isEqualTo("첫 번째 댓글");
                    assertThat(content.get(1).content()).isEqualTo("두 번째 댓글");

                    content.forEach(commentResponse -> {
                        assertThat(commentResponse.postResponse().postId()).isEqualTo(post.getId());
                        assertThat(commentResponse.userResponse().userId()).isEqualTo(user.getId());
                    });
                });

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("댓글이 없는 게시글의 댓글 목록 조회")
        void getsEmptyCommentsWhenNoComments() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            given(commentRepository.findByPostId(post.getId(), pageable))
                .willReturn(emptyPage);

            // when
            Page<CommentResponse> response = commentService.getComments(post.getId(), pageable);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getNumber()).isZero();
            assertThat(response.getSize()).isEqualTo(10);
            assertThat(response.getTotalElements()).isZero();

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("두 번째 페이지의 댓글 목록 조회")
        void getsSecondPageOfComments() {
            // given
            Pageable pageable = PageRequest.of(1, 5);
            List<Comment> comments = List.of(
                Comment.builder()
                    .content("6번째 댓글")
                    .post(post)
                    .user(user)
                    .build(),
                Comment.builder()
                    .content("7번째 댓글")
                    .post(post)
                    .user(user)
                    .build()
            );
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
                    assertThat(content.get(0).content()).isEqualTo("6번째 댓글");
                    assertThat(content.get(1).content()).isEqualTo("7번째 댓글");
                });

            then(commentRepository).should().findByPostId(post.getId(), pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("정상적인 댓글 수정")
        void updatesCommentSuccessfully() {
            // given
            UpdateCommentRequest request = new UpdateCommentRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            Comment foundComment = Comment.builder()
                .content("Original content")
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

            given(commentRepository.findById(comment.getId()))
                .willReturn(Optional.of(foundComment));

            // when
            commentService.updateComment(post.getId(), comment.getId(), request, user);

            // then
            assertThat(foundComment.getContent()).isEqualTo("Updated content");

            then(commentRepository).should().findById(comment.getId());
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시도")
        void throwsExceptionWhenCommentNotFound() {
            // given
            Long nonExistentCommentId = 999L;
            UpdateCommentRequest request = new UpdateCommentRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            given(commentRepository.findById(nonExistentCommentId))
                .willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(post.getId(), nonExistentCommentId, request, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            then(commentRepository).should().findById(nonExistentCommentId);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("권한이 없는 사용자의 댓글 수정 시도")
        void throwsExceptionWhenUnauthorizedUser() {
            // given
            UpdateCommentRequest request = new UpdateCommentRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            User unauthorizedUser = User.builder()
                .username("unauthorizedUser")
                .build();
            ReflectionTestUtils.setField(unauthorizedUser, "id", 2L);

            Comment foundComment = Comment.builder()
                .content("Original content")
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

            given(commentRepository.findById(comment.getId())).willReturn(
                Optional.of(foundComment));

            // when
            ThrowingCallable when = () ->
                commentService.updateComment(post.getId(), comment.getId(), request,
                    unauthorizedUser);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());

            then(commentRepository).should().findById(comment.getId());
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("다른 게시글의 댓글 수정 시도")
        void throwsExceptionWhenCommentNotBelongToPost() {
            // given
            UpdateCommentRequest request = new UpdateCommentRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            Post anotherPost = Post.builder()
                .content("Another post content")
                .user(user)
                .build();
            ReflectionTestUtils.setField(anotherPost, "id", 2L);

            Comment foundComment = Comment.builder()
                .content("Original content")
                .post(anotherPost)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

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
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        private static final String COMMENT_CONTENT = "Test comment";

        @Test
        @DisplayName("정상적인 댓글 삭제")
        void deletesCommentSuccessfully() {
            // given
            Comment foundComment = Comment.builder()
                .content(COMMENT_CONTENT)
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

            given(commentRepository.findById(foundComment.getId()))
                .willReturn(Optional.of(foundComment));

            // when
            commentService.deleteComment(post.getId(), foundComment.getId(), user);

            // then
            then(commentRepository).should().findById(foundComment.getId());
            then(commentRepository).should().delete(foundComment);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시도")
        void throwsExceptionWhenCommentNotFound() {
            // given
            Long nonExistentCommentId = 999L;
            given(commentRepository.findById(nonExistentCommentId)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), nonExistentCommentId, user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            then(commentRepository).should().findById(nonExistentCommentId);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("권한이 없는 사용자의 댓글 삭제 시도")
        void throwsExceptionWhenUnauthorizedUser() {
            // given
            User unauthorizedUser = User.builder()
                .username("unauthorizedUser")
                .build();
            ReflectionTestUtils.setField(unauthorizedUser, "id", 2L);

            Comment foundComment = Comment.builder()
                .content(COMMENT_CONTENT)
                .post(post)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

            given(commentRepository.findById(foundComment.getId()))
                .willReturn(Optional.of(foundComment));

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), foundComment.getId(), unauthorizedUser);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());

            then(commentRepository).should().findById(foundComment.getId());
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("다른 게시글의 댓글 삭제 시도")
        void throwsExceptionWhenCommentNotBelongToPost() {
            // given
            Post anotherPost = Post.builder()
                .content("Another post content")
                .user(user)
                .build();
            ReflectionTestUtils.setField(anotherPost, "id", 2L);

            Comment foundComment = Comment.builder()
                .content(COMMENT_CONTENT)
                .post(anotherPost)
                .user(user)
                .build();
            ReflectionTestUtils.setField(foundComment, "id", 1L);

            given(commentRepository.findById(foundComment.getId()))
                .willReturn(Optional.of(foundComment));

            // when
            ThrowingCallable when = () ->
                commentService.deleteComment(post.getId(), foundComment.getId(), user);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            then(commentRepository).should().findById(foundComment.getId());
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }
}
