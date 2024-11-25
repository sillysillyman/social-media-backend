package io.sillysillyman.api.controller.reply;

import static io.sillysillyman.api.util.MockMvcTestUtil.performDelete;
import static io.sillysillyman.api.util.MockMvcTestUtil.performGet;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPost;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPut;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.sillysillyman.core.domain.comment.CommentEntity;
import io.sillysillyman.core.domain.post.PostEntity;
import io.sillysillyman.core.domain.reply.ReplyEntity;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReplyControllerTest {

    private final static String BASE_URL = "/api/v1/comments/%d/replies";
    private final static Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    private EntityManager em;
    private Long userId;
    private Long commentId;
    private Long replyId = 1L;

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
        assert em.createQuery("SELECT COUNT(r) FROM ReplyEntity r", Long.class)
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

            CommentEntity comment = CommentEntity.builder()
                .content("comment content")
                .post(post)
                .user(user)
                .build();
            em.persist(comment);

            userId = user.getId();
            commentId = comment.getId();
        });
    }

    @AfterEach
    void tearDown() {
        withTransaction(em -> {
            em.createQuery("DELETE FROM ReplyEntity").executeUpdate();
            em.createQuery("DELETE FROM CommentEntity").executeUpdate();
            em.createQuery("DELETE FROM PostEntity").executeUpdate();
            em.createQuery("DELETE FROM UserEntity").executeUpdate();

            em.createNativeQuery("ALTER TABLE replies ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
        });
        em.close();
    }

    @DisplayName("답글 생성 API")
    @Nested
    class CreateReply {

        private static final String REQUEST_BODY = """
            {
                "content": "%s"
            }
            """;

        @DisplayName("답글 생성 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidContent_when_CreateReply_then_ReturnCreatedResponse() {
            performPost(
                mockMvc,
                BASE_URL.formatted(commentId),
                REQUEST_BODY.formatted("reply content"),
                status().isCreated(),
                jsonPath("$.data.replyId").value(replyId),
                jsonPath("$.data.content").value("reply content"),
                jsonPath("$.data.commentResponse.commentId").value(commentId),
                jsonPath("$.data.userResponse.userId").value(userId)
            );
        }

        @DisplayName("인증되지 않은 사용자의 답글 생성 실패")
        @Test
        void given_UnauthenticatedUser_when_CreateReply_then_ReturnUnauthorized() {
            performPost(
                mockMvc,
                BASE_URL.formatted(commentId),
                REQUEST_BODY.formatted("reply content"),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("빈 내용으로 답글 생성 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_EmptyContent_when_CreateReply_then_ReturnBadRequest() {
            performPost(
                mockMvc,
                BASE_URL.formatted(commentId),
                REQUEST_BODY.formatted(""),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }
    }

    @DisplayName("답글 목록 조회 API")
    @Nested
    class GetReplies {

        private Long reply1Id;
        private Long reply2Id;

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                CommentEntity comment = em.find(CommentEntity.class, commentId);

                ReplyEntity reply1 = ReplyEntity.builder()
                    .content("first reply")
                    .comment(comment)
                    .user(user)
                    .build();
                em.persist(reply1);

                ReplyEntity reply2 = ReplyEntity.builder()
                    .content("second reply")
                    .comment(comment)
                    .user(user)
                    .build();
                em.persist(reply2);

                userId = user.getId();
                commentId = comment.getId();
                reply1Id = reply1.getId();
                reply2Id = reply2.getId();
            });
        }

        @DisplayName("답글 목록 조회 성공")
        @Test
        void given_ExistingCommentId_when_GetReplies_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL.formatted(commentId),
                status().isOk(),
                jsonPath("$.content[0]").exists(),
                jsonPath("$.content[0].replyId").value(reply1Id),
                jsonPath("$.content[0].content").value("first reply"),
                jsonPath("$.content[1]").exists(),
                jsonPath("$.content[1].replyId").value(reply2Id),
                jsonPath("$.content[1].content").value("second reply")
            );
        }

        @DisplayName("존재하지 않는 댓글의 답글 목록 조회 실패")
        @Test
        void given_NonExistentCommentId_when_GetReplies_then_ReturnNotFound() {
            performGet(
                mockMvc,
                BASE_URL.formatted(NON_EXISTENT_ID),
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }
    }

    @DisplayName("답글 수정 API")
    @Nested
    class UpdateReply {

        private static final String REQUEST_BODY = """
            {
                "content": "updated reply content"
            }
            """;

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                CommentEntity comment = em.find(CommentEntity.class, commentId);

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                ReplyEntity reply = ReplyEntity.builder()
                    .content("original reply content")
                    .comment(comment)
                    .user(user)
                    .build();
                em.persist(reply);

                commentId = comment.getId();
                replyId = reply.getId();
            });
        }

        @DisplayName("답글 수정 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidContent_when_UpdateReply_then_ReturnNoContent() {
            performPut(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                REQUEST_BODY,
                status().isNoContent()
            );
            performGet(
                mockMvc,
                BASE_URL.formatted(commentId),
                status().isOk(),
                jsonPath("$.content[0]").exists(),
                jsonPath("$.content[0].content").value("updated reply content")
            );
        }

        @DisplayName("인증되지 않은 사용자의 답글 수정 실패")
        @Test
        void given_UnauthenticatedUser_when_UpdateReply_then_ReturnUnauthorized() {
            performPut(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                REQUEST_BODY,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("존재하지 않는 답글 수정 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_NonExistentCommentId_when_UpdateReply_then_ReturnNotFound() {
            performPut(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + NON_EXISTENT_ID,
                REQUEST_BODY,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @DisplayName("권한 없는 사용자의 답글 수정 실패")
        @Test
        @WithUserDetails(value = "other", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_UnauthorizedUser_when_UpdateReply_then_ReturnForbidden() {
            performPut(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                REQUEST_BODY,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }

    @DisplayName("답글 삭제 API")
    @Nested
    class DeleteReply {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);
                CommentEntity comment = em.find(CommentEntity.class, commentId);

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password1!")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                ReplyEntity reply = ReplyEntity.builder()
                    .content("reply content")
                    .comment(comment)
                    .user(user)
                    .build();
                em.persist(reply);

                commentId = comment.getId();
                replyId = reply.getId();
            });
        }

        @DisplayName("답글 삭제 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidCommentId_when_DeleteReply_then_ReturnNoContent() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                status().isNoContent()
            );
            performGet(
                mockMvc,
                BASE_URL.formatted(commentId),
                status().isOk(),
                jsonPath("$.content").isEmpty()
            );
        }

        @DisplayName("인증되지 않은 사용자의 답글 삭제 실패")
        @Test
        void given_UnauthenticatedUser_when_DeleteReply_then_ReturnUnauthorized() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @DisplayName("존재하지 않는 답글 삭제 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_NonExistentCommentId_when_DeleteReply_then_ReturnNotFound() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + NON_EXISTENT_ID,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @DisplayName("권한 없는 사용자의 답글 삭제 실패")
        @Test
        @WithUserDetails(value = "other", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_UnauthorizedUser_when_DeleteReply_then_ReturnForbidden() {
            performDelete(
                mockMvc,
                BASE_URL.formatted(commentId) + '/' + replyId,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }
}
