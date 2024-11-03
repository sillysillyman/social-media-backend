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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
