package io.sillysillyman.core.domain.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.ForbiddenAccessException;
import io.sillysillyman.core.domain.post.Post;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.post.command.CreatePostCommand;
import io.sillysillyman.core.domain.post.command.UpdatePostCommand;
import io.sillysillyman.core.domain.post.exception.PostErrorCode;
import io.sillysillyman.core.domain.post.exception.detail.PostNotFoundException;
import io.sillysillyman.core.domain.post.repository.PostRepository;
import io.sillysillyman.core.domain.user.User;
import io.sillysillyman.core.domain.user.UserEntity;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
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

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_CONTENT = "Test content";
    private static final Long DEFAULT_ID = 1L;
    private static final Long ANOTHER_ID = 2L;
    private static final Long NON_EXISTENT_ID = 999L;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;
    private UserEntity userEntity;
    private PostEntity postEntity;

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

        user = User.from(userEntity);
        post = Post.from(postEntity);
    }

    private void verifyRepositoryFindById(Long postId) {
        then(postRepository).should().findById(postId);
        then(postRepository).shouldHaveNoMoreInteractions();
    }

    private User createUnauthorizedUser() {
        UserEntity unauthorizedUserEntity = UserEntity.builder()
            .username("unauthorizedUser")
            .build();
        ReflectionTestUtils.setField(unauthorizedUserEntity, "id", ANOTHER_ID);
        return User.from(unauthorizedUserEntity);
    }

    @Nested
    @DisplayName("게시물 ID로 게시물 조회")
    class GetById {

        @Test
        @DisplayName("존재하는 게시물 ID로 조회하면 게시물 반환")
        void given_ExistingPostId_when_GetById_then_ReturnPost() {
            // given
            given(postRepository.findById(DEFAULT_ID)).willReturn(Optional.of(postEntity));

            // when
            Post foundPost = postService.getById(DEFAULT_ID);

            // then
            assertThat(foundPost)
                .satisfies(post -> {
                    assertThat(post.getId()).isEqualTo(DEFAULT_ID);
                    assertThat(post.getContent()).isEqualTo(TEST_CONTENT);
                    assertThat(post.getUser())
                        .satisfies(user -> {
                            assertThat(user.getId()).isEqualTo(DEFAULT_ID);
                            assertThat(user.getUsername()).isEqualTo(TEST_USERNAME);
                        });
                });

            verifyRepositoryFindById(post.getId());
        }

        @Test
        @DisplayName("존재하지 않는 게시물 ID로 조회하면 예외 발생")
        void given_NonExistentPostId_when_GetById_then_ThrowPostNotFoundException() {
            // given
            given(postRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable getAction = () -> postService.getById(NON_EXISTENT_ID);

            // then
            assertThatThrownBy(getAction).isInstanceOf(PostNotFoundException.class);
            verifyRepositoryFindById(NON_EXISTENT_ID);
        }
    }

    @Nested
    @DisplayName("게시물 생성")
    class CreatePost {

        private static final String NEW_POST_CONTENT = "New post content";

        @Test
        @DisplayName("유효한 요청으로 게시물 생성")
        void given_ValidPostRequest_when_CreatePost_then_PostSavedSuccessfully() {
            // given
            CreatePostCommand command = () -> NEW_POST_CONTENT;
            PostEntity savedPostEntity = PostEntity.builder()
                .content(command.getContent())
                .user(userEntity)
                .build();

            given(postRepository.save(any(PostEntity.class))).willReturn(savedPostEntity);

            // when
            Post post = postService.createPost(command, user);

            // then
            assertThat(post.getContent()).isEqualTo(NEW_POST_CONTENT);
            assertThat(post.getUser().getId()).isEqualTo(user.getId());

            then(postRepository).should().save(any(PostEntity.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }


    @Nested
    @DisplayName("게시물 조회")
    class GetPosts {

        private static final String FIRST_POST = "1st post";
        private static final String SECOND_POST = "2nd post";
        private static final int DEFAULT_PAGE_SIZE = 10;
        private static final Long TARGET_USER_ID = 2L;
        private static final String TARGET_USER_USERNAME = "targetUser";

        private void verifyRepositoryFindByUserId(Long userId, Pageable pageable) {
            then(postRepository).should().findByUserId(userId, pageable);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        private void verifyPage(
            Page<?> page,
            int expectedSize,
            int expectedNumber,
            int expectedPageSize,
            long expectedTotalElements
        ) {
            assertThat(page.getContent()).hasSize(expectedSize);
            assertThat(page.getNumber()).isEqualTo(expectedNumber);
            assertThat(page.getSize()).isEqualTo(expectedPageSize);
            assertThat(page.getTotalElements()).isEqualTo(expectedTotalElements);
        }

        private void verifyPostContents(Page<Post> pagePost, Long userId) {
            assertThat(pagePost.getContent())
                .satisfies(posts -> {
                    assertThat(posts.get(0).getContent()).isEqualTo(FIRST_POST);
                    assertThat(posts.get(1).getContent()).isEqualTo(SECOND_POST);
                    posts.forEach(post -> assertThat(post.getUser().getId()).isEqualTo(userId));
                });
        }

        private PostEntity createPostEntityWithId(
            String username,
            Long userId,
            String content,
            Long postId
        ) {
            UserEntity targetUserEntity = UserEntity.builder()
                .username(username)
                .build();
            ReflectionTestUtils.setField(targetUserEntity, "id", userId);

            PostEntity newPostEntity = PostEntity.builder()
                .content(content)
                .user(targetUserEntity)
                .build();
            ReflectionTestUtils.setField(newPostEntity, "id", postId);

            return newPostEntity;
        }

        @Nested
        @DisplayName("사용자의 게시물 목록 페이징 조회")
        class GetUserPosts {

            private static final String SIXTH_POST = "6th post";
            private static final String SEVENTH_POST = "7th post";

            @Test
            @DisplayName("사용자 게시물 목록 페이지네이션 조회")
            void given_PostsWithPagination_when_GetPosts_then_ReturnPaginatedPosts() {
                // given
                Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
                List<PostEntity> postEntities = List.of(
                    createPostEntityWithId(
                        TARGET_USER_USERNAME,
                        TARGET_USER_ID,
                        FIRST_POST,
                        DEFAULT_ID
                    ),
                    createPostEntityWithId(
                        TARGET_USER_USERNAME,
                        TARGET_USER_ID,
                        SECOND_POST,
                        ANOTHER_ID
                    )
                );
                Page<PostEntity> postEntityPage = new PageImpl<>(
                    postEntities,
                    pageable,
                    postEntities.size()
                );

                given(postRepository.findByUserId(TARGET_USER_ID, pageable))
                    .willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(TARGET_USER_ID, pageable);

                // then
                verifyPage(postPage, 2, 0, DEFAULT_PAGE_SIZE, 2);
                verifyPostContents(postPage, TARGET_USER_ID);
                verifyRepositoryFindByUserId(TARGET_USER_ID, pageable);
            }

            @Test
            @DisplayName("게시물이 없는 사용자의 게시물 목록 조회")
            void given_UserWithNoPosts_when_GetUserPosts_then_ReturnEmptyPage() {
                // given
                Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
                Page<PostEntity> emptyPostEntityPage = new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
                );

                given(postRepository.findByUserId(TARGET_USER_ID, pageable))
                    .willReturn(emptyPostEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(TARGET_USER_ID, pageable);

                // then
                verifyPage(postPage, 0, 0, DEFAULT_PAGE_SIZE, 0);
                verifyRepositoryFindByUserId(TARGET_USER_ID, pageable);
            }

            @Test
            @DisplayName("두 번째 페이지의 게시물 목록 조회")
            void given_MultiplePages_when_GetSecondPage_then_ReturnCorrectPageOfPosts() {
                // given
                Pageable pageable = PageRequest.of(1, 5);
                List<PostEntity> postEntities = List.of(
                    createPostEntityWithId(
                        TARGET_USER_USERNAME,
                        TARGET_USER_ID,
                        SIXTH_POST,
                        DEFAULT_ID
                    ),
                    createPostEntityWithId(
                        TARGET_USER_USERNAME,
                        TARGET_USER_ID,
                        SEVENTH_POST,
                        ANOTHER_ID
                    )
                );

                postEntities.forEach(postEntity ->
                    ReflectionTestUtils.setField(
                        postEntity,
                        "id",
                        postEntities.indexOf(postEntity) + 6L
                    )
                );

                Page<PostEntity> postEntityPage = new PageImpl<>(postEntities, pageable, 7);

                given(postRepository.findByUserId(TARGET_USER_ID, pageable))
                    .willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(TARGET_USER_ID, pageable);

                // then
                verifyPage(postPage, 2, 1, 5, 7);

                assertThat(postPage.getContent())
                    .satisfies(content -> {
                        assertThat(content.get(0).getContent()).isEqualTo(SIXTH_POST);
                        assertThat(content.get(1).getContent()).isEqualTo(SEVENTH_POST);
                    });

                verifyRepositoryFindByUserId(TARGET_USER_ID, pageable);
            }
        }

        @Nested
        @DisplayName("본인 게시물 목록 페이징 조회")
        class GetMyPosts {

            @Test
            @DisplayName("현재 사용자의 게시물 목록을 페이징하여 반환")
            void given_AuthenticatedUser_when_GetMyPosts_then_ReturnPaginatedPosts() {
                // given
                Pageable pageable = PageRequest.of(
                    0,
                    DEFAULT_PAGE_SIZE,
                    Sort.by("createdAt").descending()
                );
                List<PostEntity> postEntities = List.of(
                    createPostEntityWithId(TEST_USERNAME, DEFAULT_ID, FIRST_POST, DEFAULT_ID),
                    createPostEntityWithId(TEST_USERNAME, DEFAULT_ID, SECOND_POST, DEFAULT_ID)
                );
                Page<PostEntity> postEntityPage = new PageImpl<>(postEntities, pageable, 2);

                given(postRepository.findByUserId(user.getId(), pageable))
                    .willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getMyPosts(user, pageable);

                // then
                assertThat(postPage.getContent().getFirst().getUser().getId())
                    .isEqualTo(user.getId());

                verifyPage(postPage, 2, 0, DEFAULT_PAGE_SIZE, 2);
                verifyPostContents(postPage, user.getId());
                verifyRepositoryFindByUserId(user.getId(), pageable);
            }
        }
    }

    @Nested
    @DisplayName("게시물 수정")
    class UpdatePost {

        private static final String UPDATED_CONTENT = "Updated content";

        @Test
        @DisplayName("게시물 작성자가 수정 성공")
        void given_ValidOwner_when_UpdatePost_then_UpdateSuccessfully() {
            // given
            UpdatePostCommand command = () -> UPDATED_CONTENT;
            PostEntity updatedPostEntity = PostEntity.builder()
                .content(UPDATED_CONTENT)
                .user(userEntity)
                .build();
            ReflectionTestUtils.setField(updatedPostEntity, "id", DEFAULT_ID);

            given(postRepository.findById(postEntity.getId()))
                .willReturn(Optional.of(postEntity));
            given(postRepository.save(any(PostEntity.class)))
                .willReturn(updatedPostEntity);

            // when
            postService.updatePost(post.getId(), command, user);
            Post updatedPost = Post.from(updatedPostEntity);

            // then
            assertThat(updatedPost.getContent()).isEqualTo(UPDATED_CONTENT);

            then(postRepository).should().save(any(PostEntity.class));
            verifyRepositoryFindById(post.getId());
        }

        @Test
        @DisplayName("존재하지 않는 게시물 수정 시도")
        void given_NonExistentPostId_when_UpdatePost_then_ThrowPostNotFoundException() {
            // given

            UpdatePostCommand command = () -> UPDATED_CONTENT;

            given(postRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> postService.updatePost(NON_EXISTENT_ID, command, user);

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 게시물 수정 시도")
        void given_UnauthorizedUser_when_UpdatePost_then_ThrowForbiddenAccessException() {
            // given
            UpdatePostCommand command = () -> UPDATED_CONTENT;
            User unauthorizedUser = createUnauthorizedUser();

            given(postRepository.findById(postEntity.getId()))
                .willReturn(Optional.of(postEntity));

            // when
            ThrowingCallable when = () ->
                postService.updatePost(
                    postEntity.getId(),
                    command,
                    unauthorizedUser
                );

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            verifyRepositoryFindById(post.getId());
        }
    }

    @Nested
    @DisplayName("게시물 삭제")
    class DeletePostEntity {

        @Test
        @DisplayName("정상적인 게시물 삭제")
        void given_ValidPost_when_DeletePost_then_DeleteSuccessfully() {
            // given
            given(postRepository.findById(postEntity.getId()))
                .willReturn(Optional.of(postEntity));

            // when
            postService.deletePost(post.getId(), user);

            // then
            verifyRepositoryDelete(post.getId());
        }

        @Test
        @DisplayName("존재하지 않는 게시물 삭제 시도")
        void given_NonExistentPostId_when_DeletePost_then_ThrowPostNotFoundException() {
            // given
            given(postRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> postService.deletePost(NON_EXISTENT_ID, user);

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());

            verifyRepositoryFindById(NON_EXISTENT_ID);
        }

        @Test
        @DisplayName("권한이 없는 사용자의 게시물 삭제 시도")
        void given_UnauthorizedUser_when_DeletePost_then_ThrowForbiddenAccessException() {
            // given
            User unauthorizedUser = createUnauthorizedUser();

            given(postRepository.findById(postEntity.getId()))
                .willReturn(Optional.of(postEntity));

            // when
            ThrowingCallable when = () -> postService.deletePost(post.getId(), unauthorizedUser);

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            verifyRepositoryFindById(post.getId());
        }

        private void verifyRepositoryDelete(Long postId) {
            then(postRepository).should().findById(postId);
            then(postRepository).should().delete(any(PostEntity.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }
}
