package io.sillysillyman.socialmediabackend.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import io.sillysillyman.socialmediabackend.auth.exception.detail.UnauthorizedAccessException;
import io.sillysillyman.socialmediabackend.domain.post.Post;
import io.sillysillyman.socialmediabackend.domain.post.dto.CreatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
import io.sillysillyman.socialmediabackend.domain.post.dto.UpdatePostRequest;
import io.sillysillyman.socialmediabackend.domain.post.exception.detail.PostNotFoundException;
import io.sillysillyman.socialmediabackend.domain.post.repository.PostRepository;
import io.sillysillyman.socialmediabackend.domain.user.User;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder()
            .username("testUser")
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        post = Post.builder()
            .content("Test content")
            .user(user)
            .build();
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    @Nested
    @DisplayName("게시물 ID로 게시물 조회")
    class GetById {

        @Test
        @DisplayName("존재하는 게시물 ID로 조회하면 게시물 반환")
        void returnsPostWhenExists() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            Post foundPost = postService.getById(1L);

            // then
            assertThat(foundPost).isEqualTo(post);
            then(postRepository).should().findById(1L);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 게시물 ID로 조회하면 PostNotFoundException 발생")
        void throwsExceptionWhenNotExists() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when
            ThrowingCallable getAction = () -> postService.getById(999L);

            // then
            assertThatThrownBy(getAction).isInstanceOf(PostNotFoundException.class);
            then(postRepository).should().findById(999L);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("게시물 생성")
    class CreatePost {

        @Test
        @DisplayName("유효한 요청으로 게시물 생성")
        void createsPostWithValidRequest() {
            // given
            CreatePostRequest request = new CreatePostRequest();
            ReflectionTestUtils.setField(request, "content", "New post content");

            Post savedPost = Post.builder()
                .content(request.getContent())
                .user(user)
                .build();
            given(postRepository.save(any(Post.class))).willReturn(savedPost);

            // when
            PostResponse response = postService.createPost(request, user);

            // then
            assertThat(response.content()).isEqualTo("New post content");
            assertThat(response.userResponse().id()).isEqualTo(user.getId());
            then(postRepository).should().save(any(Post.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("사용자의 게시물 목록 페이징 조회")
    class GetUserPosts {

        @Test
        @DisplayName("사용자 게시물 목록 페이징 조회")
        void returnsPagedUserPosts() {
            // given
            Post post1 = Post.builder()
                .content("First post")
                .user(user)
                .build();
            Post post2 = Post.builder()
                .content("Second post")
                .user(user)
                .build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            List<Post> posts = List.of(post1, post2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 2);

            given(postRepository.findByUserId(user.getId(), pageable)).willReturn(postPage);

            // when
            Page<PostResponse> result = postService.getUserPosts(user.getId(), pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).content()).isEqualTo("First post");
            assertThat(result.getContent().get(1).content()).isEqualTo("Second post");
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(postRepository).should().findByUserId(user.getId(), pageable);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("본인 게시물 목록 페이징 조회")
    class GetMyPosts {

        @Test
        @DisplayName("현재 사용자의 게시물 목록을 페이징하여 반환")
        void returnsPagedMyPosts() {
            // given
            Post post1 = Post.builder()
                .content("My first post")
                .user(user)
                .build();
            Post post2 = Post.builder()
                .content("My second post")
                .user(user)
                .build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            List<Post> posts = List.of(post1, post2);
            Page<Post> postPage = new PageImpl<>(posts, pageable, 2);

            given(postRepository.findByUserId(user.getId(), pageable)).willReturn(postPage);

            // when
            Page<PostResponse> result = postService.getMyPosts(user, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).content()).isEqualTo("My first post");
            assertThat(result.getContent().get(1).content()).isEqualTo("My second post");
            assertThat(result.getContent().get(0).userResponse().id()).isEqualTo(user.getId());
            assertThat(result.getTotalElements()).isEqualTo(2);
            then(postRepository).should().findByUserId(user.getId(), pageable);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("게시물 수정")
    class UpdatePost {

        @Test
        @DisplayName("게시물 작성자가 수정 성공")
        void updatesPostWhenOwner() {
            // given
            UpdatePostRequest request = new UpdatePostRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            Post existingPost = Post.builder()
                .content("Original content")
                .user(user)
                .build();

            given(postRepository.findById(1L)).willReturn(Optional.of(existingPost));

            // when
            postService.updatePost(1L, request, user);

            // then
            assertThat(existingPost.getContent()).isEqualTo("Updated content");
            then(postRepository).should().findById(1L);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("게시물 작성자가 아닌 사용자가 수정하면 UnauthorizedAccessException 발생")
        void throwsExceptionWhenNotOwner() {
            // given
            User otherUser = User.builder()
                .username("otherUser")
                .build();
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            UpdatePostRequest request = new UpdatePostRequest();
            ReflectionTestUtils.setField(request, "content", "Updated content");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            ThrowingCallable updateAction = () -> postService.updatePost(1L, request, otherUser);

            // then
            assertThatThrownBy(updateAction).isInstanceOf(UnauthorizedAccessException.class);
            assertThat(post.getContent()).isEqualTo("Test content");
            then(postRepository).should().findById(1L);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("게시물 삭제")
    class DeletePost {

        @Test
        @DisplayName("게시물 작성자가 삭제 성공")
        void deletesPostWhenOwner() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            postService.deletePost(1L, user);

            // then
            then(postRepository).should().findById(1L);
            then(postRepository).should().delete(post);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("게시물 작성자가 아닌 사용자가 삭제하면 UnauthorizedAccessException 발생")
        void throwsExceptionWhenNotOwner() {
            // given
            User otherUser = User.builder()
                .username("otherUser")
                .build();
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            ThrowingCallable deleteAction = () -> postService.deletePost(1L, otherUser);

            // then
            assertThatThrownBy(deleteAction).isInstanceOf(UnauthorizedAccessException.class);
            then(postRepository).should().findById(1L);
            then(postRepository).should(never()).delete(any(Post.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }
}
