//package io.sillysillyman.core.domain.comment.service;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.BDDMockito.then;
//
//import io.sillysillyman.core.auth.exception.AuthErrorCode;
//import io.sillysillyman.core.auth.exception.detail.UnauthorizedAccessException;
//import io.sillysillyman.core.domain.comment.CommentEntity;
//import io.sillysillyman.core.domain.comment.exception.CommentErrorCode;
//import io.sillysillyman.core.domain.comment.exception.detail.CommentNotBelongToPostException;
//import io.sillysillyman.core.domain.comment.exception.detail.CommentNotFoundException;
//import io.sillysillyman.core.domain.comment.repository.CommentRepository;
//import io.sillysillyman.core.domain.post.PostEntity;
//import io.sillysillyman.core.domain.post.service.PostService;
//import io.sillysillyman.core.domain.user.UserEntity;
//import io.sillysillyman.socialmediabackend.domain.comment.dto.CommentResponse;
//import io.sillysillyman.socialmediabackend.domain.comment.dto.CreateCommentRequest;
//import io.sillysillyman.socialmediabackend.domain.comment.dto.UpdateCommentRequest;
//import java.util.Collections;
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
//import org.springframework.test.util.ReflectionTestUtils;
//
//@ExtendWith(MockitoExtension.class)
//public class CommentServiceTest {
//
//    private static final String TEST_USERNAME = "testUser";
//    private static final String TEST_CONTENT = "Test content";
//    private static final Long DEFAULT_ID = 1L;
//    private static final Long ANOTHER_ID = 2L;
//    private static final Long NON_EXISTENT_ID = 999L;
//
//    @Mock
//    private CommentRepository commentRepository;
//
//    @Mock
//    private PostService postService;
//
//    @InjectMocks
//    private CommentService commentService;
//
//    private UserEntity userEntity;
//    private PostEntity postEntity;
//    private CommentEntity commentEntity;
//
//    @BeforeEach
//    void setUp() {
//        userEntity = UserEntity.builder()
//            .username(TEST_USERNAME)
//            .build();
//        ReflectionTestUtils.setField(userEntity, "id", DEFAULT_ID);
//
//        postEntity = PostEntity.builder()
//            .content(TEST_CONTENT)
//            .user(userEntity)
//            .build();
//        ReflectionTestUtils.setField(postEntity, "id", DEFAULT_ID);
//
//        commentEntity = CommentEntity.builder()
//            .content(TEST_CONTENT)
//            .post(postEntity)
//            .user(userEntity)
//            .build();
//        ReflectionTestUtils.setField(commentEntity, "id", DEFAULT_ID);
//    }
//
//    private void verifyRepositoryFindById(Long commentId) {
//        then(commentRepository).should().findById(commentId);
//        then(commentRepository).shouldHaveNoMoreInteractions();
//    }
//
//    private UserEntity createUnauthorizedUser() {
//        UserEntity unauthorizedUserEntity = UserEntity.builder()
//            .username("unauthorizedUser")
//            .build();
//        ReflectionTestUtils.setField(unauthorizedUserEntity, "id", ANOTHER_ID);
//        return unauthorizedUserEntity;
//    }
//
//    @Nested
//    @DisplayName("댓글 생성")
//    class CreateCommentEntity {
//
//        private static final String NEW_COMMENT_CONTENT = "New comment content";
//
//        @Test
//        @DisplayName("유효한 요청으로 댓글 생성")
//        void createsCommentWithValidRequest() {
//            // given
//            CreateCommentRequest request = new CreateCommentRequest();
//            ReflectionTestUtils.setField(request, "content", NEW_COMMENT_CONTENT);
//
//            CommentEntity savedCommentEntity = CommentEntity.builder()
//                .content(NEW_COMMENT_CONTENT)
//                .post(postEntity)
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(savedCommentEntity, "id", DEFAULT_ID);
//
//            given(postService.getById(postEntity.getId())).willReturn(postEntity);
//            given(commentRepository.save(any(CommentEntity.class))).willReturn(savedCommentEntity);
//
//            // when
//            CommentResponse response = commentService.createComment(postEntity.getId(), request,
//                userEntity);
//
//            // then
//            assertThat(response.content()).isEqualTo(NEW_COMMENT_CONTENT);
//            assertThat(response.postResponse().postId()).isEqualTo(postEntity.getId());
//            assertThat(response.userResponse().userId()).isEqualTo(userEntity.getId());
//
//            then(commentRepository).should().save(any(CommentEntity.class));
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//
//    @Nested
//    @DisplayName("댓글 목록 조회")
//    class GetComments {
//
//        private static final String FIRST_COMMENT = "1st comment";
//        private static final String SECOND_COMMENT = "2nd comment";
//        private static final String SIXTH_COMMENT = "6th comment";
//        private static final String SEVENTH_COMMENT = "7th comment";
//        private static final int DEFAULT_PAGE_SIZE = 10;
//
//        @Test
//        @DisplayName("게시글의 댓글 목록을 페이지네이션과 함께 조회")
//        void getsCommentsWithPagination() {
//            // given
//            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
//            List<CommentEntity> commentEntities = createCommentsList(FIRST_COMMENT, SECOND_COMMENT);
//            Page<CommentEntity> commentPage = new PageImpl<>(commentEntities, pageable,
//                commentEntities.size());
//
//            given(commentRepository.findByPostId(postEntity.getId(), pageable)).willReturn(
//                commentPage);
//
//            // when
//            Page<CommentResponse> response = commentService.getComments(postEntity.getId(),
//                pageable);
//
//            // then
//            verifyPageResponse(response, 2, 0, DEFAULT_PAGE_SIZE);
//            verifyCommentContents(response);
//            verifyRepositoryFindByPostId(pageable);
//        }
//
//        @Test
//        @DisplayName("댓글이 없는 게시글의 댓글 목록 조회")
//        void getsEmptyCommentsWhenNoComments() {
//            // given
//            Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
//            Page<CommentEntity> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
//
//            given(commentRepository.findByPostId(postEntity.getId(), pageable)).willReturn(
//                emptyPage);
//
//            // when
//            Page<CommentResponse> response = commentService.getComments(postEntity.getId(),
//                pageable);
//
//            // then
//            assertThat(response.getContent()).isEmpty();
//            assertThat(response.getNumber()).isZero();
//            assertThat(response.getSize()).isEqualTo(DEFAULT_PAGE_SIZE);
//            assertThat(response.getTotalElements()).isZero();
//
//            then(commentRepository).should().findByPostId(postEntity.getId(), pageable);
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//
//        @Test
//        @DisplayName("두 번째 페이지의 댓글 목록 조회")
//        void getsSecondPageOfComments() {
//            // given
//            Pageable pageable = PageRequest.of(1, 5);
//            List<CommentEntity> commentEntities = createCommentsList(SIXTH_COMMENT,
//                SEVENTH_COMMENT);
//
//            commentEntities.forEach(commentEntity ->
//                ReflectionTestUtils.setField(
//                    commentEntity, "id", commentEntities.indexOf(commentEntity) + 6L)
//            );
//
//            Page<CommentEntity> commentPage = new PageImpl<>(commentEntities, pageable, 7);
//
//            given(commentRepository.findByPostId(postEntity.getId(), pageable)).willReturn(
//                commentPage);
//
//            // when
//            Page<CommentResponse> response = commentService.getComments(postEntity.getId(),
//                pageable);
//
//            // then
//            assertThat(response.getContent()).hasSize(2);
//            assertThat(response.getNumber()).isEqualTo(1);
//            assertThat(response.getSize()).isEqualTo(5);
//            assertThat(response.getTotalElements()).isEqualTo(7);
//
//            assertThat(response.getContent())
//                .satisfies(content -> {
//                    assertThat(content.get(0).content()).isEqualTo(SIXTH_COMMENT);
//                    assertThat(content.get(1).content()).isEqualTo(SEVENTH_COMMENT);
//                });
//
//            then(commentRepository).should().findByPostId(postEntity.getId(), pageable);
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//
//        private void verifyRepositoryFindByPostId(Pageable pageable) {
//            then(commentRepository).should().findByPostId(postEntity.getId(), pageable);
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//
//        private void verifyPageResponse(
//            Page<?> page,
//            int expectedSize,
//            int expectedNumber,
//            int expectedPageSize
//        ) {
//            assertThat(page.getContent()).hasSize(expectedSize);
//            assertThat(page.getNumber()).isEqualTo(expectedNumber);
//            assertThat(page.getSize()).isEqualTo(expectedPageSize);
//        }
//
//        private void verifyCommentContents(Page<CommentResponse> response) {
//            assertThat(response.getContent())
//                .satisfies(content -> {
//                    assertThat(content.get(0).content()).isEqualTo(FIRST_COMMENT);
//                    assertThat(content.get(1).content()).isEqualTo(SECOND_COMMENT);
//                    content.forEach(this::verifyCommentResponse);
//                });
//        }
//
//        private void verifyCommentResponse(CommentResponse response) {
//            assertThat(response.postResponse().postId()).isEqualTo(postEntity.getId());
//            assertThat(response.userResponse().userId()).isEqualTo(userEntity.getId());
//        }
//
//        private CommentEntity createCommentWithId(String content, Long id) {
//            CommentEntity newCommentEntity = CommentEntity.builder()
//                .content(content)
//                .post(postEntity)
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(newCommentEntity, "id", id);
//            return newCommentEntity;
//        }
//
//        private List<CommentEntity> createCommentsList(String content1, String content2) {
//            return List.of(
//                createCommentWithId(content1, DEFAULT_ID),
//                createCommentWithId(content2, ANOTHER_ID)
//            );
//        }
//    }
//
//    @Nested
//    @DisplayName("댓글 수정")
//    class UpdateCommentEntity {
//
//        private static final String UPDATED_CONTENT = "Updated content";
//
//        @Test
//        @DisplayName("정상적인 댓글 수정")
//        void updatesCommentSuccessfully() {
//            // given
//            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
//            given(commentRepository.findById(commentEntity.getId())).willReturn(Optional.of(
//                commentEntity));
//
//            // when
//            commentService.updateComment(postEntity.getId(), commentEntity.getId(), request,
//                userEntity);
//
//            // then
//            assertThat(commentEntity.getContent()).isEqualTo(UPDATED_CONTENT);
//            verifyRepositoryFindById(commentEntity.getId());
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 댓글 수정 시도")
//        void throwsExceptionWhenCommentNotFound() {
//            // given
//
//            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
//
//            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.updateComment(postEntity.getId(), NON_EXISTENT_ID, request,
//                    userEntity);
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(CommentNotFoundException.class)
//                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
//
//            then(commentRepository).should().findById(NON_EXISTENT_ID);
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//
//        @Test
//        @DisplayName("권한이 없는 사용자의 댓글 수정 시도")
//        void throwsExceptionWhenUnauthorizedUser() {
//            // given
//            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
//            UserEntity unauthorizedUserEntity = createUnauthorizedUser();
//
//            given(commentRepository.findById(commentEntity.getId())).willReturn(Optional.of(
//                commentEntity));
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.updateComment(
//                    postEntity.getId(),
//                    commentEntity.getId(),
//                    request,
//                    unauthorizedUserEntity
//                );
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(UnauthorizedAccessException.class)
//                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());
//
//            verifyRepositoryFindById(commentEntity.getId());
//        }
//
//        @Test
//        @DisplayName("다른 게시글의 댓글 수정 시도")
//        void throwsExceptionWhenCommentNotBelongToPost() {
//            // given
//            UpdateCommentRequest request = createUpdateRequest(UPDATED_CONTENT);
//
//            PostEntity anotherPostEntity = PostEntity.builder()
//                .content("Another post content")
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(anotherPostEntity, "id", ANOTHER_ID);
//
//            CommentEntity foundCommentEntity = CommentEntity.builder()
//                .content("Original content")
//                .post(anotherPostEntity)
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(foundCommentEntity, "id", DEFAULT_ID);
//
//            given(commentRepository.findById(commentEntity.getId()))
//                .willReturn(Optional.of(foundCommentEntity));
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.updateComment(postEntity.getId(), commentEntity.getId(), request,
//                    userEntity);
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(CommentNotBelongToPostException.class)
//                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());
//
//            then(commentRepository).should().findById(commentEntity.getId());
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//
//        private UpdateCommentRequest createUpdateRequest(String content) {
//            UpdateCommentRequest request = new UpdateCommentRequest();
//            ReflectionTestUtils.setField(request, "content", content);
//            return request;
//        }
//    }
//
//    @Nested
//    @DisplayName("댓글 삭제")
//    class DeleteCommentEntity {
//
//        @Test
//        @DisplayName("정상적인 댓글 삭제")
//        void deletesCommentSuccessfully() {
//            // given
//            given(commentRepository.findById(commentEntity.getId())).willReturn(Optional.of(
//                commentEntity));
//
//            // when
//            commentService.deleteComment(postEntity.getId(), commentEntity.getId(), userEntity);
//
//            // then
//            verifyRepositoryDelete(commentEntity.getId());
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 댓글 삭제 시도")
//        void throwsExceptionWhenCommentNotFound() {
//            // given
//            given(commentRepository.findById(NON_EXISTENT_ID)).willReturn(Optional.empty());
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.deleteComment(postEntity.getId(), NON_EXISTENT_ID, userEntity);
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(CommentNotFoundException.class)
//                .hasMessage(CommentErrorCode.COMMENT_NOT_FOUND.getMessage());
//
//            verifyRepositoryFindById(NON_EXISTENT_ID);
//        }
//
//        @Test
//        @DisplayName("권한이 없는 사용자의 댓글 삭제 시도")
//        void throwsExceptionWhenUnauthorizedUser() {
//            // given
//            UserEntity unauthorizedUserEntity = createUnauthorizedUser();
//            given(commentRepository.findById(commentEntity.getId())).willReturn(Optional.of(
//                commentEntity));
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.deleteComment(postEntity.getId(), commentEntity.getId(),
//                    unauthorizedUserEntity);
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(UnauthorizedAccessException.class)
//                .hasMessage(AuthErrorCode.UNAUTHORIZED_ACCESS.getMessage());
//
//            verifyRepositoryFindById(commentEntity.getId());
//        }
//
//        @Test
//        @DisplayName("다른 게시글의 댓글 삭제 시도")
//        void throwsExceptionWhenCommentNotBelongToPost() {
//            // given
//            PostEntity anotherPostEntity = PostEntity.builder()
//                .content(TEST_CONTENT)
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(anotherPostEntity, "id", ANOTHER_ID);
//
//            CommentEntity commentEntityInAnotherPost = CommentEntity.builder()
//                .content(TEST_CONTENT)
//                .post(anotherPostEntity)
//                .user(userEntity)
//                .build();
//            ReflectionTestUtils.setField(commentEntityInAnotherPost, "id", commentEntity.getId());
//
//            given(commentRepository.findById(commentEntity.getId()))
//                .willReturn(Optional.of(commentEntityInAnotherPost));
//
//            // when
//            ThrowingCallable when = () ->
//                commentService.deleteComment(postEntity.getId(), commentEntity.getId(), userEntity);
//
//            // then
//            assertThatThrownBy(when)
//                .isInstanceOf(CommentNotBelongToPostException.class)
//                .hasMessage(CommentErrorCode.COMMENT_NOT_BELONG_TO_POST.getMessage());
//
//            verifyRepositoryFindById(commentEntity.getId());
//        }
//
//        private void verifyRepositoryDelete(Long commentId) {
//            then(commentRepository).should().findById(commentId);
//            then(commentRepository).should().delete(commentEntity);
//            then(commentRepository).shouldHaveNoMoreInteractions();
//        }
//    }
//}
