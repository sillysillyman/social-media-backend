//package io.sillysillyman.core.domain.post.service;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//import static org.mockito.Mockito.never;
//
//import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
//import io.sillysillyman.core.domain.post.PostEntity;
//import io.sillysillyman.core.domain.post.exception.detail.PostNotFoundException;
//import io.sillysillyman.core.domain.post.repository.PostRepository;
//import io.sillysillyman.core.domain.user.UserEntity;
//import io.sillysillyman.socialmediabackend.domain.post.dto.CreatePostRequest;
//import io.sillysillyman.socialmediabackend.domain.post.dto.PostResponse;
//import io.sillysillyman.socialmediabackend.domain.post.dto.UpdatePostRequest;
//import java.util.List;
//import java.util.Optional;
//import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//class PostServiceTest {
//
//    @Mock
//    private PostRepository postRepository;
//
//    @InjectMocks
//    private PostService postService;
//
//    private UserEntity userEntity;
//    private PostEntity postEntity;
//
//    @BeforeEach
//    void setUp() {
//        userEntity = UserEntity.builder()
//            .username("testUser")
//            .build();
//        ReflectionTestUtils.setField(userEntity, "id", 1L);
//
//        postEntity = PostEntity.builder()
//            .content("Test content")
//            .user(userEntity)
//            .build();
//        ReflectionTestUtils.setField(postEntity, "id", 1L);
//    }
//
//    @Nested
//    @DisplayName("게시물 ID로 게시물 조회")
//    class GetById {
//
//        @Test
//        @DisplayName("존재하는 게시물 ID로 조회하면 게시물 반환")
//        void returnsPostWhenExists() {
//            // given
//            given(postRepository.findById(1L)).willReturn(Optional.of(postEntity));
//
//            // when
//            PostEntity foundPostEntity = postService.getById(1L);
//
//            // then
//            assertThat(foundPostEntity).isEqualTo(postEntity);
//            then(postRepository).should().findById(1L);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 게시물 ID로 조회하면 PostNotFoundException 발생")
//        void throwsExceptionWhenNotExists() {
//            // given
//            given(postRepository.findById(999L)).willReturn(Optional.empty());
//
//            // when
//            ThrowingCallable getAction = () -> postService.getById(999L);
//
//            // then
//            assertThatThrownBy(getAction).isInstanceOf(PostNotFoundException.class);
//            then(postRepository).should().findById(999L);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("게시물 생성")
//    class CreatePostEntity {
//
//        @Test
//        @DisplayName("유효한 요청으로 게시물 생성")
//        void createsPostWithValidRequest() {
//            // given
//            CreatePostRequest request = new CreatePostRequest();
//            ReflectionTestUtils.setField(request, "content", "New post content");
//
//            PostEntity savedPostEntity = PostEntity.builder()
//                .content(request.getContent())
//                .user(userEntity)
//                .build();
//            given(postRepository.save(any(PostEntity.class))).willReturn(savedPostEntity);
//
//            // when
//            PostResponse response = postService.createPost(request, userEntity);
//
//            // then
//            assertThat(response.content()).isEqualTo("New post content");
//            assertThat(response.userResponse().userId()).isEqualTo(userEntity.getId());
//            then(postRepository).should().save(any(PostEntity.class));
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("사용자의 게시물 목록 페이징 조회")
//    class GetUserPostsEntity {
//
//        @Test
//        @DisplayName("사용자 게시물 목록 페이징 조회")
//        void returnsPagedUserPosts() {
//            // given
//            PostEntity postEntity1 = PostEntity.builder()
//                .content("First post")
//                .user(userEntity)
//                .build();
//            PostEntity postEntity2 = PostEntity.builder()
//                .content("Second post")
//                .user(userEntity)
//                .build();
//
//            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//            List<PostEntity> postEntities = List.of(postEntity1, postEntity2);
//            Page<PostEntity> postPage = new PageImpl<>(postEntities, pageable, 2);
//
//            given(postRepository.findByUserId(userEntity.getId(), pageable)).willReturn(postPage);
//
//            // when
//            Page<PostResponse> result = postService.getUserPosts(userEntity.getId(), pageable);
//
//            // then
//            assertThat(result.getContent()).hasSize(2);
//            assertThat(result.getContent().get(0).content()).isEqualTo("First post");
//            assertThat(result.getContent().get(1).content()).isEqualTo("Second post");
//            assertThat(result.getTotalElements()).isEqualTo(2);
//            then(postRepository).should().findByUserId(userEntity.getId(), pageable);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("본인 게시물 목록 페이징 조회")
//    class GetMyPosts {
//
//        @Test
//        @DisplayName("현재 사용자의 게시물 목록을 페이징하여 반환")
//        void returnsPagedMyPosts() {
//            // given
//            PostEntity postEntity1 = PostEntity.builder()
//                .content("My first post")
//                .user(userEntity)
//                .build();
//            PostEntity postEntity2 = PostEntity.builder()
//                .content("My second post")
//                .user(userEntity)
//                .build();
//
//            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//            List<PostEntity> postEntities = List.of(postEntity1, postEntity2);
//            Page<PostEntity> postPage = new PageImpl<>(postEntities, pageable, 2);
//
//            given(postRepository.findByUserId(userEntity.getId(), pageable)).willReturn(postPage);
//
//            // when
//            Page<PostResponse> result = postService.getMyPosts(userEntity, pageable);
//
//            // then
//            assertThat(result.getContent()).hasSize(2);
//            assertThat(result.getContent().get(0).content()).isEqualTo("My first post");
//            assertThat(result.getContent().get(1).content()).isEqualTo("My second post");
//            assertThat(result.getContent().get(0).userResponse().userId()).isEqualTo(
//                userEntity.getId());
//            assertThat(result.getTotalElements()).isEqualTo(2);
//            then(postRepository).should().findByUserId(userEntity.getId(), pageable);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("게시물 수정")
//    class UpdatePostEntity {
//
//        @Test
//        @DisplayName("게시물 작성자가 수정 성공")
//        void updatesPostWhenOwner() {
//            // given
//            UpdatePostRequest request = new UpdatePostRequest();
//            ReflectionTestUtils.setField(request, "content", "Updated content");
//
//            PostEntity existingPostEntity = PostEntity.builder()
//                .content("Original content")
//                .user(userEntity)
//                .build();
//
//            given(postRepository.findById(1L)).willReturn(Optional.of(existingPostEntity));
//
//            // when
//            postService.updatePost(1L, request, userEntity);
//
//            // then
//            assertThat(existingPostEntity.getContent()).isEqualTo("Updated content");
//            then(postRepository).should().findById(1L);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//
//        @Test
//        @DisplayName("게시물 작성자가 아닌 사용자가 수정하면 UnauthorizedAccessException 발생")
//        void throwsExceptionWhenNotOwner() {
//            // given
//            UserEntity otherUserEntity = UserEntity.builder()
//                .username("otherUser")
//                .build();
//            ReflectionTestUtils.setField(otherUserEntity, "id", 2L);
//
//            UpdatePostRequest request = new UpdatePostRequest();
//            ReflectionTestUtils.setField(request, "content", "Updated content");
//
//            given(postRepository.findById(1L)).willReturn(Optional.of(postEntity));
//
//            // when
//            ThrowingCallable updateAction = () -> postService.updatePost(1L, request,
//                otherUserEntity);
//
//            // then
//            assertThatThrownBy(updateAction).isInstanceOf(UnauthorizedAccessException.class);
//            assertThat(postEntity.getContent()).isEqualTo("Test content");
//            then(postRepository).should().findById(1L);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("게시물 삭제")
//    class DeletePostEntity {
//
//        @Test
//        @DisplayName("게시물 작성자가 삭제 성공")
//        void deletesPostWhenOwner() {
//            // given
//            given(postRepository.findById(1L)).willReturn(Optional.of(postEntity));
//
//            // when
//            postService.deletePost(1L, userEntity);
//
//            // then
//            then(postRepository).should().findById(1L);
//            then(postRepository).should().delete(postEntity);
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//
//        @Test
//        @DisplayName("게시물 작성자가 아닌 사용자가 삭제하면 UnauthorizedAccessException 발생")
//        void throwsExceptionWhenNotOwner() {
//            // given
//            UserEntity otherUserEntity = UserEntity.builder()
//                .username("otherUser")
//                .build();
//            ReflectionTestUtils.setField(otherUserEntity, "id", 2L);
//
//            given(postRepository.findById(1L)).willReturn(Optional.of(postEntity));
//
//            // when
//            ThrowingCallable deleteAction = () -> postService.deletePost(1L, otherUserEntity);
//
//            // then
//            assertThatThrownBy(deleteAction).isInstanceOf(UnauthorizedAccessException.class);
//            then(postRepository).should().findById(1L);
//            then(postRepository).should(never()).delete(any(PostEntity.class));
//            then(postRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//}
