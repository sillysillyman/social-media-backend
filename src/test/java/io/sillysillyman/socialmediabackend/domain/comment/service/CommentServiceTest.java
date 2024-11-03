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
                .content(request.getContent())
                .user(user)
                .build();
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);

            // when
            CommentResponse response = commentService.createComment(request, user);

            // then
            assertThat(response.content()).isEqualTo("New comment content");
            assertThat(response.userResponse().id()).isEqualTo(user.getId());
            then(commentRepository).should().save(any(Comment.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }
}
