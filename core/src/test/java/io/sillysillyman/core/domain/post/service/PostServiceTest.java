package io.sillysillyman.core.domain.post.service;

import static io.sillysillyman.core.common.constants.TestConstants.ANOTHER_POST_ID;
import static io.sillysillyman.core.common.constants.TestConstants.BASE_TIME;
import static io.sillysillyman.core.common.constants.TestConstants.CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.DEFAULT_PAGE_SIZE;
import static io.sillysillyman.core.common.constants.TestConstants.FIRST_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.NON_EXISTENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.POST_ID;
import static io.sillysillyman.core.common.constants.TestConstants.SECOND_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.UPDATED_CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.USERNAME;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ID;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createPostEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUnauthorizedUserEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUserEntity;
import static io.sillysillyman.core.common.utils.TestUtils.assertPageProperties;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private UserEntity userEntity;
    private PostEntity postEntity;

    @BeforeEach
    void setUp() {
        userEntity = createUserEntity();
        postEntity = createPostEntity(userEntity);

        user = User.from(userEntity);
    }

    @DisplayName("게시물 ID로 게시물 조회")
    @Nested
    class GetById {

        @DisplayName("게시물 조회 성공")
        @Test
        void given_ExistingPostId_when_GetById_then_ReturnPost() {
            // given
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(postEntity));

            // when
            Post foundPost = postService.getById(POST_ID);

            // then
            assertThat(foundPost)
                .satisfies(post -> {
                    assertThat(post.getId()).isEqualTo(POST_ID);
                    assertThat(post.getContent()).isEqualTo(CONTENT);
                    assertThat(post.getUser())
                        .satisfies(user -> {
                            assertThat(user.getId()).isEqualTo(USER_ID);
                            assertThat(user.getUsername()).isEqualTo(USERNAME);
                        });
                });

            then(postRepository).should().findById(POST_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("게시물 조회 실패")
        @Test
        void given_NonExistentPostId_when_GetById_then_ThrowPostNotFoundException() {
            // given
            given(postRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> postService.getById(NON_EXISTENT_ID);

            // then
            assertThatThrownBy(when)
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());
            ;

            then(postRepository).should().findById(NON_EXISTENT_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("게시물 생성")
    @Nested
    class CreatePost {

        @DisplayName("게시물 생성 성공")
        @Test
        void given_ValidCommand_when_CreatePost_then_ReturnSavedPost() {
            // given
            CreatePostCommand command = () -> CONTENT;

            PostEntity savedPostEntity = createPostEntity(userEntity);

            given(postRepository.save(any(PostEntity.class))).willReturn(savedPostEntity);

            // when
            Post post = postService.createPost(command, user);

            // then
            assertThat(post.getContent()).isEqualTo(CONTENT);
            assertThat(post.getUser().getId()).isEqualTo(USER_ID);

            then(postRepository).should().save(any(PostEntity.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("게시물 목록 조회")
    @Nested
    class GetPosts {

        private static final String OLDER_POST_CONTENT = "older post content";
        private static final String NEWER_POST_CONTENT = "newer post content";
        private static final Instant OLDER_POST_CREATED_AT = BASE_TIME;
        private static final Instant NEWER_POST_CREATED_AT = BASE_TIME.plus(1, ChronoUnit.DAYS);

        @Nested
        @DisplayName("사용자 게시물 목록 조회")
        class GetUserPosts {

            private static final String SECOND_PAGE_OLDER_POST_CONTENT = "second page older post content";
            private static final String SECOND_PAGE_NEWER_POST_CONTENT = "second page newer post content";

            @Test
            @DisplayName("사용자 게시물 목록 페이지 조회")
            void given_UserWithPosts_when_GetPosts_then_ReturnPageOfPosts() {
                // given
                Pageable pageable = PageRequest.of(
                    FIRST_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE,
                    Sort.by("createdAt").descending()
                );

                List<PostEntity> postEntities = List.of(
                    createPostEntity(
                        ANOTHER_POST_ID,
                        NEWER_POST_CONTENT,
                        NEWER_POST_CREATED_AT,
                        userEntity
                    ),
                    createPostEntity(
                        POST_ID,
                        OLDER_POST_CONTENT,
                        OLDER_POST_CREATED_AT,
                        userEntity
                    )
                );
                Page<PostEntity> postEntityPage = new PageImpl<>(
                    postEntities,
                    pageable,
                    postEntities.size()
                );

                given(postRepository.findByUserId(USER_ID, pageable)).willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(USER_ID, pageable);

                // then
                assertPageProperties(postPage, 2, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 2,
                    posts -> {
                        assertThat(posts.get(0).getContent()).isEqualTo(NEWER_POST_CONTENT);
                        assertThat(posts.get(1).getContent()).isEqualTo(OLDER_POST_CONTENT);
                        posts.forEach(
                            post -> assertThat(post.getUser().getId()).isEqualTo(USER_ID));
                    }
                );

                then(postRepository).should().findByUserId(USER_ID, pageable);
                then(postRepository).shouldHaveNoMoreInteractions();
            }

            @Test
            @DisplayName("게시물이 없는 사용자의 게시물 목록 조회")
            void given_UserWithNoPosts_when_GetUserPosts_then_ReturnEmptyPage() {
                // given
                Pageable pageable = PageRequest.of(
                    FIRST_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE,
                    Sort.by("createdAt").descending()
                );

                Page<PostEntity> emptyPostEntityPage = new PageImpl<>(
                    Collections.emptyList(),
                    pageable,
                    0
                );

                given(postRepository.findByUserId(USER_ID, pageable))
                    .willReturn(emptyPostEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(USER_ID, pageable);

                // then
                assertPageProperties(postPage, 0, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 0);

                then(postRepository).should().findByUserId(USER_ID, pageable);
                then(postRepository).shouldHaveNoMoreInteractions();
            }

            @Test
            @DisplayName("두 번째 페이지의 게시물 목록 조회")
            void given_MultiplePages_when_GetSecondPage_then_ReturnSecondPageOfPosts() {
                // given
                Pageable pageable = PageRequest.of(
                    SECOND_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE,
                    Sort.by("createdAt").descending()
                );

                List<PostEntity> postEntities = List.of(
                    createPostEntity(
                        ANOTHER_POST_ID,
                        SECOND_PAGE_NEWER_POST_CONTENT,
                        NEWER_POST_CREATED_AT,
                        userEntity
                    ),
                    createPostEntity(
                        POST_ID,
                        SECOND_PAGE_OLDER_POST_CONTENT,
                        OLDER_POST_CREATED_AT,
                        userEntity
                    )
                );

                Page<PostEntity> postEntityPage = new PageImpl<>(postEntities, pageable, 12);

                given(postRepository.findByUserId(USER_ID, pageable)).willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getUserPosts(USER_ID, pageable);

                // then
                assertPageProperties(postPage, 2, SECOND_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 12,
                    content -> {
                        assertThat(content.get(0).getContent()).isEqualTo(
                            SECOND_PAGE_NEWER_POST_CONTENT);
                        assertThat(content.get(1).getContent()).isEqualTo(
                            SECOND_PAGE_OLDER_POST_CONTENT);
                        content.forEach(
                            post -> assertThat(post.getUser().getId()).isEqualTo(USER_ID)
                        );
                    }
                );

                then(postRepository).should().findByUserId(USER_ID, pageable);
                then(postRepository).shouldHaveNoMoreInteractions();
            }
        }

        @Nested
        @DisplayName("본인 게시물 목록 조회")
        class GetMyPosts {

            @Test
            @DisplayName("본인 게시물 목록 페이지 반환")
            void given_AuthenticatedUser_when_GetMyPosts_then_ReturnPageOfPosts() {
                // given
                Pageable pageable = PageRequest.of(
                    FIRST_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE,
                    Sort.by("createdAt").descending()
                );

                List<PostEntity> postEntities = List.of(
                    createPostEntity(
                        ANOTHER_POST_ID,
                        NEWER_POST_CONTENT,
                        NEWER_POST_CREATED_AT,
                        userEntity
                    ),
                    createPostEntity(
                        POST_ID,
                        OLDER_POST_CONTENT,
                        OLDER_POST_CREATED_AT,
                        userEntity
                    )
                );

                Page<PostEntity> postEntityPage = new PageImpl<>(postEntities, pageable, 2);

                given(postRepository.findByUserId(USER_ID, pageable)).willReturn(postEntityPage);

                // when
                Page<Post> postPage = postService.getMyPosts(user, pageable);

                // then
                assertPageProperties(postPage, 2, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 2,
                    content -> {
                        assertThat(content.get(0).getContent()).isEqualTo(NEWER_POST_CONTENT);
                        assertThat(content.get(1).getContent()).isEqualTo(OLDER_POST_CONTENT);
                        content.forEach(
                            post -> assertThat(post.getUser().getId()).isEqualTo(USER_ID)
                        );
                    }
                );

                then(postRepository).should().findByUserId(USER_ID, pageable);
                then(postRepository).shouldHaveNoMoreInteractions();
            }
        }
    }

    @Nested
    @DisplayName("게시물 수정")
    class UpdatePost {

        @Test
        @DisplayName("게시물 작성자가 수정 성공")
        void given_ValidCommand_when_UpdatePost_then_PostUpdatedSuccessfully() {
            // given
            UpdatePostCommand command = () -> UPDATED_CONTENT;

            PostEntity updatedPostEntity = createPostEntity(UPDATED_CONTENT, userEntity);

            given(postRepository.findById(POST_ID)).willReturn(Optional.of(postEntity));
            given(postRepository.save(any(PostEntity.class))).willReturn(updatedPostEntity);

            // when
            postService.updatePost(POST_ID, command, user);
            Post updatedPost = Post.from(updatedPostEntity);

            // then
            assertThat(updatedPost.getContent()).isEqualTo(UPDATED_CONTENT);

            then(postRepository).should().save(any(PostEntity.class));
            then(postRepository).should().findById(POST_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 게시물 수정 실패")
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

            then(postRepository).should().findById(NON_EXISTENT_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("권한이 없는 사용자의 게시물 수정 시도")
        void given_UnauthorizedUser_when_UpdatePost_then_ThrowForbiddenAccessException() {
            // given
            UpdatePostCommand command = () -> UPDATED_CONTENT;

            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(postRepository.findById(POST_ID)).willReturn(Optional.of(postEntity));

            // when
            ThrowingCallable when = () -> postService.updatePost(
                POST_ID,
                command,
                unauthorizedUser
            );

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(postRepository).should().findById(POST_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("게시물 삭제")
    class DeletePostEntity {

        @Test
        @DisplayName("게시물 삭제 성공")
        void given_ExistingPost_when_DeletePost_then_DeleteSuccessfully() {
            // given
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(postEntity));

            // when
            postService.deletePost(POST_ID, user);

            // then
            then(postRepository).should().findById(POST_ID);
            then(postRepository).should().delete(any(PostEntity.class));
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("존재하지 않는 게시물 삭제 실패")
        void given_NonExistentPostId_when_DeletePost_then_ThrowPostNotFoundException() {
            // given
            given(postRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> postService.deletePost(NON_EXISTENT_ID, user);

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(PostNotFoundException.class)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());

            then(postRepository).should().findById(NON_EXISTENT_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("권한이 없는 사용자의 게시물 삭제 시도")
        void given_UnauthorizedUser_when_DeletePost_then_ThrowForbiddenAccessException() {
            // given
            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(postRepository.findById(POST_ID)).willReturn(Optional.of(postEntity));

            // when
            ThrowingCallable when = () -> postService.deletePost(POST_ID, unauthorizedUser);

            // then
            Assertions.assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(postRepository).should().findById(POST_ID);
            then(postRepository).shouldHaveNoMoreInteractions();
        }
    }
}
