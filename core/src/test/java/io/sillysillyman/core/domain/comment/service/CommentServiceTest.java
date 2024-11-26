package io.sillysillyman.core.domain.comment.service;

import static io.sillysillyman.core.common.constants.TestConstants.ANOTHER_COMMENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.ANOTHER_POST_ID;
import static io.sillysillyman.core.common.constants.TestConstants.COMMENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.DEFAULT_PAGE_SIZE;
import static io.sillysillyman.core.common.constants.TestConstants.FIRST_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.NON_EXISTENT_ID;
import static io.sillysillyman.core.common.constants.TestConstants.POST_ID;
import static io.sillysillyman.core.common.constants.TestConstants.SECOND_PAGE_NUMBER;
import static io.sillysillyman.core.common.constants.TestConstants.UPDATED_CONTENT;
import static io.sillysillyman.core.common.constants.TestConstants.USER_ID;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createCommentEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createPostEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUnauthorizedUserEntity;
import static io.sillysillyman.core.common.fixtures.TestFixtures.createUserEntity;
import static io.sillysillyman.core.common.utils.TestUtils.assertPageProperties;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import io.sillysillyman.core.auth.exception.AuthErrorCode;
import io.sillysillyman.core.auth.exception.detail.ForbiddenAccessException;
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
    private UserEntity userEntity;
    private PostEntity postEntity;
    private CommentEntity commentEntity;

    @BeforeEach
    void setUp() {
        userEntity = createUserEntity();
        postEntity = createPostEntity(userEntity);
        commentEntity = createCommentEntity(postEntity, userEntity);

        user = User.from(userEntity);
        post = Post.from(postEntity);
    }

    @DisplayName("댓글 생성")
    @Nested
    class CreateComment {

        @DisplayName("유효한 요청으로 댓글 생성")
        @Test
        void given_ValidCommand_when_CreateComment_then_ReturnSavedComment() {
            // given
            CreateCommentCommand command = () -> CONTENT;

            CommentEntity savedCommentEntity = createCommentEntity(postEntity, userEntity);

            given(postService.getById(POST_ID)).willReturn(post);
            given(commentRepository.save(any(CommentEntity.class))).willReturn(savedCommentEntity);

            // when
            Comment comment = commentService.createComment(POST_ID, command, user);

            // then
            assertThat(comment.getContent()).isEqualTo(CONTENT);
            assertThat(comment.getPost().getId()).isEqualTo(POST_ID);
            assertThat(comment.getUser().getId()).isEqualTo(USER_ID);

            then(commentRepository).should().save(any(CommentEntity.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("댓글 목록 조회")
    @Nested
    class GetComments {

        private static final String OLDER_COMMENT_CONTENT = "older comment content";
        private static final String NEWER_COMMENT_CONTENT = "newer comment content";
        private static final String SECOND_PAGE_OLDER_COMMENT_CONTENT = "second page older comment content";
        private static final String SECOND_PAGE_NEWER_COMMENT_CONTENT = "second page newer comment content";

        @DisplayName("게시물의 댓글 목록 조회")
        @Test
        void given_PostWithComments_when_GetComments_then_ReturnPageOfComments() {
            // given
            Pageable pageable = PageRequest.of(FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            List<CommentEntity> commentEntities = List.of(
                createCommentEntity(
                    COMMENT_ID,
                    OLDER_COMMENT_CONTENT,
                    postEntity,
                    userEntity
                ),
                createCommentEntity(
                    ANOTHER_COMMENT_ID,
                    NEWER_COMMENT_CONTENT,
                    postEntity,
                    userEntity
                )
            );

            Page<CommentEntity> commentEntityPage = new PageImpl<>(
                commentEntities,
                pageable,
                commentEntities.size()
            );

            given(commentRepository.findByPostId(POST_ID, pageable)).willReturn(commentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(POST_ID, pageable);

            // then
            assertPageProperties(commentPage, 2, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 2,
                content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(OLDER_COMMENT_CONTENT);
                    assertThat(content.get(1).getContent()).isEqualTo(NEWER_COMMENT_CONTENT);
                    content.forEach(comment -> {
                            assertThat(comment.getPost().getId()).isEqualTo(POST_ID);
                            assertThat(comment.getUser().getId()).isEqualTo(USER_ID);
                        }
                    );
                }
            );

            then(commentRepository).should().findByPostId(POST_ID, pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("댓글이 없는 게시물의 댓글 목록 조회")
        @Test
        void given_PostWithNoComments_when_GetComments_then_ReturnEmptyPage() {
            // given
            Pageable pageable = PageRequest.of(FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            Page<CommentEntity> emptyCommentEntityPage = new PageImpl<>(
                Collections.emptyList(),
                pageable,
                0
            );

            given(commentRepository.findByPostId(POST_ID, pageable))
                .willReturn(emptyCommentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(POST_ID, pageable);

            // then
            assertPageProperties(commentPage, 0, FIRST_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 0);

            then(commentRepository).should().findByPostId(POST_ID, pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("게시물의 두 번째 페이지 댓글 목록 조회")
        @Test
        void given_MultipleComments_when_GetSecondPage_then_ReturnSecondPageOfComments() {
            // given
            Pageable pageable = PageRequest.of(SECOND_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

            List<CommentEntity> commentEntities = List.of(
                createCommentEntity(
                    COMMENT_ID,
                    SECOND_PAGE_OLDER_COMMENT_CONTENT,
                    postEntity,
                    userEntity
                ),
                createCommentEntity(
                    ANOTHER_COMMENT_ID,
                    SECOND_PAGE_NEWER_COMMENT_CONTENT,
                    postEntity,
                    userEntity
                )
            );

            Page<CommentEntity> commentEntityPage = new PageImpl<>(commentEntities, pageable, 12);

            given(commentRepository.findByPostId(POST_ID, pageable))
                .willReturn(commentEntityPage);

            // when
            Page<Comment> commentPage = commentService.getComments(POST_ID, pageable);

            // then
            assertPageProperties(commentPage, 2, SECOND_PAGE_NUMBER, DEFAULT_PAGE_SIZE, 12,
                content -> {
                    assertThat(content.get(0).getContent()).isEqualTo(
                        SECOND_PAGE_OLDER_COMMENT_CONTENT
                    );
                    assertThat(content.get(1).getContent()).isEqualTo(
                        SECOND_PAGE_NEWER_COMMENT_CONTENT
                    );
                    content.forEach(comment -> {
                            assertThat(comment.getPost().getId()).isEqualTo(POST_ID);
                            assertThat(comment.getUser().getId()).isEqualTo(USER_ID);
                        }
                    );
                }
            );

            then(commentRepository).should().findByPostId(POST_ID, pageable);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("댓글 수정")
    @Nested
    class UpdateComment {

        @DisplayName("댓글 수정 성공")
        @Test
        void given_ValidCommand_when_UpdateComment_then_CommentUpdatedSuccessfully() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            CommentEntity updatedCommentEntity = createCommentEntity(
                UPDATED_CONTENT,
                postEntity,
                userEntity
            );

            given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(commentEntity));
            given(commentRepository.save(any(CommentEntity.class)))
                .willReturn(updatedCommentEntity);

            // when
            commentService.updateComment(POST_ID, COMMENT_ID, command, user);
            Comment updatedComment = Comment.from(updatedCommentEntity);

            // then
            assertThat(updatedComment.getContent()).isEqualTo(UPDATED_CONTENT);

            then(commentRepository).should().save(any(CommentEntity.class));
            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("존재하지 않는 댓글 수정 실패")
        @Test
        void given_NonExistentComment_when_UpdateComment_then_ThrowCommentNotFoundException() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> commentService.updateComment(
                POST_ID,
                NON_EXISTENT_ID,
                command,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            then(commentRepository).should().findById(NON_EXISTENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("권한이 없는 사용자의 댓글 수정 실패")
        @Test
        void given_UnauthorizedUser_when_UpdateComment_then_ThrowForbiddenAccessException() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () -> commentService.updateComment(
                POST_ID,
                COMMENT_ID,
                command,
                unauthorizedUser
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("다른 게시물의 댓글 수정 실패")
        @Test
        void given_CommentFromDifferentPost_when_UpdateComment_then_ThrowCommentNotBelongToPostException() {
            // given
            UpdateCommentCommand command = () -> UPDATED_CONTENT;

            given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () -> commentService.updateComment(
                ANOTHER_POST_ID,
                COMMENT_ID,
                command,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }

    @DisplayName("댓글 삭제")
    @Nested
    class DeleteComment {

        @DisplayName("댓글 삭제 성공")
        @Test
        void given_ExistingComment_when_DeleteComment_then_CommentDeletedSuccessfully() {
            // given
            given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(commentEntity));

            // when
            commentService.deleteComment(POST_ID, COMMENT_ID, user);

            // then
            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).should().delete(any(CommentEntity.class));
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("존재하지 않는 댓글 삭제 실패")
        @Test
        void given_NonExistentComment_when_DeleteComment_then_ThrowCommentNotFoundException() {
            // given
            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());

            // when
            ThrowingCallable when = () -> commentService.deleteComment(
                POST_ID,
                NON_EXISTENT_ID,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());

            then(commentRepository).should().findById(NON_EXISTENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("권한이 없는 사용자의 댓글 삭제 시도")
        @Test
        void given_UnauthorizedUser_when_DeleteComment_then_ThrowForbiddenAccessException() {
            // given
            UserEntity unauthorizedUserEntity = createUnauthorizedUserEntity();
            User unauthorizedUser = User.from(unauthorizedUserEntity);

            given(commentRepository.findById(commentEntity.getId()))
                .willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () -> commentService.deleteComment(
                POST_ID,
                COMMENT_ID,
                unauthorizedUser
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessage(AuthErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }

        @DisplayName("다른 게시물의 댓글 삭제 실패")
        @Test
        void given_CommentFromDifferentPost_when_DeleteComment_then_ThrowCommentNotBelongToPostException() {
            // given
            given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(commentEntity));

            // when
            ThrowingCallable when = () -> commentService.deleteComment(
                ANOTHER_POST_ID,
                COMMENT_ID,
                user
            );

            // then
            assertThatThrownBy(when)
                .isInstanceOf(CommentNotBelongToPostException.class)
                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());

            then(commentRepository).should().findById(COMMENT_ID);
            then(commentRepository).shouldHaveNoMoreInteractions();
        }
    }
}
