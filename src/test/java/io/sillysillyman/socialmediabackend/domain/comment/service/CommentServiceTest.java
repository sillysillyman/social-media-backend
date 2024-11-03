package io.sillysillyman.socialmediabackend.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.socialmediabackend.domain.comment.Comment;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CommentResponse;
import io.sillysillyman.socialmediabackend.domain.comment.dto.CreateCommentRequest;
import io.sillysillyman.socialmediabackend.domain.comment.repository.CommentRepository;
import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.post.service.PostService;
import io.sillysillyman.socialmediabackend.domain.user.User;
import java.util.Collections;
import java.util.List;
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
}