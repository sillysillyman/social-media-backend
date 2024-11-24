package io.sillysillyman.api.controller.comment;

import static io.sillysillyman.api.util.MockMvcTestUtil.performDelete;
import static io.sillysillyman.api.util.MockMvcTestUtil.performGet;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPost;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPut;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.user.UserEntity;
import io.sillysillyman.core.domain.user.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class CommentControllerTest {

    private final static String BASE_URL = "/api/v1/posts/%d/comments";
    private final static Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    private EntityManager em;
    private Long userId;
    private Long postId;
    private Long commentId = 1L;

    private void withTransaction(Consumer<EntityManager> block) {
        em.getTransaction().begin();
        block.accept(em);
        em.flush();
        em.getTransaction().commit();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();

        assert em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
            .getSingleResult() == 0;
        assert em.createQuery("SELECT COUNT(p) FROM PostEntity p", Long.class)
            .getSingleResult() == 0;
        assert em.createQuery("SELECT COUNT(c) FROM CommentEntity c", Long.class)
            .getSingleResult() == 0;

        withTransaction(em -> {
            UserEntity user = UserEntity.builder()
                .username("tester")
                .password("password1!")
                .role(UserRole.USER)
                .build();
            em.persist(user);

            PostEntity post = PostEntity.builder()
                .content("post content")
                .user(user)
                .build();
            em.persist(post);

            userId = user.getId();
            postId = post.getId();
        });
    }

    @AfterEach
    void tearDown() {
        withTransaction(em -> {
            em.createQuery("DELETE FROM CommentEntity").executeUpdate();
            em.createQuery("DELETE FROM PostEntity").executeUpdate();
            em.createQuery("DELETE FROM UserEntity").executeUpdate();

            em.createNativeQuery("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
        });
        em.close();
    }

    @DisplayName("댓글 생성 API")
    @Nested
    class CreateComment {

        private static final String REQUEST_BODY = """
            {
                "content": "%s"
            }
            """;

        @DisplayName("댓글 생성 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidContent_when_CreateComment_then_ReturnCreatedResponse() {
            performPost(
                mockMvc,
                BASE_URL.formatted(postId),
                REQUEST_BODY.formatted("comment content"),
                status().isCreated(),
                jsonPath("$.data.commentId").value(commentId),
                jsonPath("$.data.content").value("comment content"),
                jsonPath("$.data.postResponse").exists(),
                jsonPath("$.data.postResponse.postId").value(postId),
                jsonPath("$.data.postResponse.content").value("post content"),
                jsonPath("$.data.userResponse").exists(),
                jsonPath("$.data.userResponse.userId").value(userId),
                jsonPath("$.data.userResponse.username").value("tester")
            );
        }

        @DisplayName("인증되지 않은 사용자의 댓글 생성 실패")
        @Test
        void given_UnauthenticatedUser_when_CreateComment_then_ReturnUnauthorized() {
            performPost(
                mockMvc,
                BASE_URL.formatted(postId),
                REQUEST_BODY.formatted("comment content"),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("빈 내용으로 댓글 생성 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_EmptyContent_when_CreateComment_then_ReturnBadRequest() {
            performPost(
                mockMvc,
                BASE_URL.formatted(postId),
                REQUEST_BODY.formatted(""),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }
    }

    @DisplayName("댓글 목록 조회 API")
    @Nested
    class GetComments {

        private Long comment1Id;
        private Long comment2Id;

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                PostEntity post = em.find(PostEntity.class, postId);

                CommentEntity comment1 = CommentEntity.builder()
                    .content("first comment")
                    .post(post)
                    .user(user)
                    .build();
                em.persist(comment1);

                CommentEntity comment2 = CommentEntity.builder()
                    .content("second comment")
                    .post(post)
                    .user(user)
                    .build();
                em.persist(comment2);

                userId = user.getId();
                postId = post.getId();
                comment1Id = comment1.getId();
                comment2Id = comment2.getId();
            });
        }

        @DisplayName("댓글 목록 조회 성공")
        @Test
        void given_ExistingPostId_when_GetComments_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL.formatted(postId),
                status().isOk(),
                jsonPath("$.content[0]").exists(),
                jsonPath("$.content[0].commentId").value(comment1Id),
                jsonPath("$.content[0].content").value("first comment"),
                jsonPath("$.content[1]").exists(),
                jsonPath("$.content[1].commentId").value(comment2Id),
                jsonPath("$.content[1].content").value("second comment")
            );
        }

        @DisplayName("존재하지 않는 게시물의 댓글 목록 조회 실패")
        @Test
        void given_NonExistentPostId_when_GetComments_then_ReturnNotFound() {
            performGet(
                mockMvc,
                BASE_URL.formatted(NON_EXISTENT_ID),
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }
    }

    @DisplayName("댓글 수정 API")
    @Nested
    class UpdateComment {

        private static final String REQUEST_BODY = """
            {
                "content": "updated comment content"
            }
            """;

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                PostEntity post = em.find(PostEntity.class, postId);

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                CommentEntity comment = CommentEntity.builder()
                    .content("original comment content")
                    .post(post)
                    .user(user)
                    .build();
                em.persist(comment);

                postId = post.getId();
                commentId = comment.getId();
            });
        }

        @DisplayName("댓글 수정 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidContent_when_UpdateComment_then_ReturnNoContent() {
            performPut(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                REQUEST_BODY,
                status().isNoContent()
            );
            performGet(
                mockMvc,
                BASE_URL.formatted(postId),
                status().isOk(),
                jsonPath("$.content[0]").exists(),
                jsonPath("$.content[0].content").value("updated comment content")
            );
        }

        @DisplayName("인증되지 않은 사용자의 댓글 수정 실패")
        @Test
        void given_UnauthenticatedUser_when_UpdateComment_then_ReturnUnauthorized() {
            performPut(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                REQUEST_BODY,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("존재하지 않는 댓글 수정 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_NonExistentPostId_when_UpdateComment_then_ReturnNotFound() {
            performPut(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + NON_EXISTENT_ID,
                REQUEST_BODY,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @DisplayName("권한 없는 사용자의 댓글 수정 실패")
        @Test
        @WithUserDetails(value = "other", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_UnauthorizedUser_when_UpdateComment_then_ReturnForbidden() {
            performPut(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                REQUEST_BODY,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }

    @DisplayName("댓글 삭제 API")
    @Nested
    class DeleteComment {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                PostEntity post = em.find(PostEntity.class, postId);

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password1!")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                CommentEntity comment = CommentEntity.builder()
                    .content("comment content")
                    .post(post)
                    .user(user)
                    .build();
                em.persist(comment);

                postId = post.getId();
                commentId = comment.getId();
            });
        }

        @DisplayName("댓글 삭제 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidPostId_when_DeleteComment_then_ReturnNoContent() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                status().isNoContent()
            );
            performGet(
                mockMvc,
                BASE_URL.formatted(postId),
                status().isOk(),
                jsonPath("$.content").isEmpty()
            );
        }

        @DisplayName("인증되지 않은 사용자의 댓글 삭제 실패")
        @Test
        void given_UnauthenticatedUser_when_DeleteComment_then_ReturnUnauthorized() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("존재하지 않는 댓글 삭제 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_NonExistentPostId_when_DeleteComment_then_ReturnNotFound() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + NON_EXISTENT_ID,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @DisplayName("권한 없는 사용자의 댓글 삭제 실패")
        @Test
        @WithUserDetails(value = "other", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_UnauthorizedUser_when_DeleteComment_then_ReturnForbidden() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(postId) + '/' + commentId,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }
}
