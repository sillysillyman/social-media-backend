package io.sillysillyman.api.controller.post;

import static io.sillysillyman.api.util.MockMvcTestUtil.performDelete;
import static io.sillysillyman.api.util.MockMvcTestUtil.performGet;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPost;
import static io.sillysillyman.api.util.MockMvcTestUtil.performPut;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostControllerTest {

    private final static String BASE_URL = "/api/v1/posts";
    private final static Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    private EntityManager em;
    private Long userId;
    private Long postId = 1L;

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

        withTransaction(em -> {
            UserEntity user = UserEntity.builder()
                .username("tester")
                .password("password")
                .role(UserRole.USER)
                .build();
            em.persist(user);

            userId = user.getId();
        });
    }

    @AfterEach
    void tearDown() {
        withTransaction(em -> {
            em.createQuery("DELETE FROM PostEntity").executeUpdate();
            em.createQuery("DELETE FROM UserEntity").executeUpdate();

            em.createNativeQuery("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
            em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
        });
        em.close();
    }

    @Nested
    @DisplayName("게시물 생성 API")
    class CreatePost {

        @Test
        @DisplayName("게시물 생성 성공")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_ValidContent_when_CreatePost_ReturnCreatedResponse() {
            String requestBody = """
                {
                    "content": "Test Content"
                }
                """;

            performPost(
                mockMvc,
                BASE_URL,
                requestBody,
                status().isCreated(),
                jsonPath("$.data.postId").value(postId),
                jsonPath("$.data.content").value("Test Content"),
                jsonPath("$.data.userResponse").exists(),
                jsonPath("$.data.userResponse.userId").value(userId),
                jsonPath("$.data.userResponse.username").value("tester")
            );
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 게시물 생성 실패")
        void given_UnauthenticatedUser_when_CreatePost_then_ReturnUnauthorized() {
            String requestBody = """
                {
                    "content": "Test Content"
                }
                """;

            performPost(
                mockMvc,
                BASE_URL,
                requestBody,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @Test
        @DisplayName("빈 내용으로 게시물 생성 실패")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_EmptyContent_when_CreatePost_then_ReturnBadRequest() {
            String requestBody = """
                {
                    "content": ""
                }
                """;

            performPost(
                mockMvc,
                BASE_URL,
                requestBody,
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }
    }

    @Nested
    @DisplayName("게시글 조회 API")
    class GetPost {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.username = :username",
                        UserEntity.class
                    )
                    .setParameter("username", "tester")
                    .getSingleResult();

                PostEntity post = PostEntity.builder()
                    .content("Test Content")
                    .user(user)
                    .build();
                em.persist(post);

                userId = user.getId();
                postId = post.getId();
            });
        }

        @Test
        @DisplayName("게시물 조회 성공")
        void given_ExistingPostId_when_GetPost_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL + '/' + postId,
                status().isOk(),
                jsonPath("$.data.postId").value(postId),
                jsonPath("$.data.content").value("Test Content"),
                jsonPath("$.data.userResponse.userId").value(userId),
                jsonPath("$.data.userResponse.username").value("tester")
            );
        }

        @Test
        @DisplayName("존재하지 않는 게시물 조회 실패")
        void given_NonExistentPostId_when_GetPost_then_ReturnNotFound() {
            performGet(
                mockMvc,
                BASE_URL + '/' + NON_EXISTENT_ID,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }
    }

    @Nested
    @DisplayName("게시물 수정 API")
    class UpdatePost {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.username = :username",
                        UserEntity.class
                    )
                    .setParameter("username", "tester")
                    .getSingleResult();

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                PostEntity post = PostEntity.builder()
                    .content("Original Content")
                    .user(user)
                    .build();
                em.persist(post);

                postId = post.getId();
            });
        }

        @Test
        @DisplayName("게시물 수정 성공")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_ValidContent_when_UpdatePost_then_ReturnNoContent() {
            String requestBody = """
                {
                    "content": "Updated Content"
                }
                """;

            performPut(mockMvc, BASE_URL + '/' + postId, requestBody, status().isNoContent());
            performGet(
                mockMvc,
                BASE_URL + '/' + postId,
                status().isOk(),
                jsonPath("$.data.content").value("Updated Content")
            );
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 게시물 수정 실패")
        void given_UnauthenticatedUser_when_UpdatePost_then_ReturnUnauthorized() {
            String requestBody = """
                {
                    "content": "Updated Content"
                }
                """;

            performPut(
                mockMvc,
                BASE_URL + '/' + postId,
                requestBody,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @Test
        @DisplayName("존재하지 않는 게시물 수정 실패")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_NonExistentPostId_when_UpdatePost_then_ReturnNotFound() {
            String requestBody = """
                {
                    "content": "Updated Content"
                }
                """;

            performPut(
                mockMvc,
                BASE_URL + '/' + NON_EXISTENT_ID,
                requestBody,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @Test
        @DisplayName("권한 없는 사용자의 게시글 수정 실패")
        @WithUserDetails(
            value = "other",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_UnauthorizedUser_when_UpdatePost_then_ReturnForbidden() {
            String requestBody = """
                {
                    "content": "Updated Content"
                }
                """;

            performPut(
                mockMvc,
                BASE_URL + '/' + postId,
                requestBody,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }

    @Nested
    @DisplayName("게시물 삭제 API")
    class DeletePost {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.username = :username",
                        UserEntity.class
                    )
                    .setParameter("username", "tester")
                    .getSingleResult();

                UserEntity other = UserEntity.builder()
                    .username("other")
                    .password("password")
                    .role(UserRole.USER)
                    .build();
                em.persist(other);

                PostEntity post = PostEntity.builder()
                    .content("Test Content")
                    .user(user)
                    .build();
                em.persist(post);

                postId = post.getId();
            });
        }

        @Test
        @DisplayName("게시물 삭제 성공")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_ValidPostId_when_DeletePost_then_ReturnNoContent() {
            performDelete(mockMvc, BASE_URL + '/' + postId, status().isNoContent());
            performGet(mockMvc, "/api/v1/posts/1", status().isNotFound());
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 게시물 삭제 실패")
        void given_UnauthenticatedUser_when_DeletePost_then_ReturnUnauthorized() {
            performDelete(
                mockMvc,
                BASE_URL + '/' + postId,
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @Test
        @DisplayName("존재하지 않는 게시물 삭제 실패")
        @WithUserDetails(
            value = "tester",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_NonExistentPostId_when_DeletePost_then_ReturnNotFound() {
            performDelete(
                mockMvc,
                BASE_URL + '/' + NON_EXISTENT_ID,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }

        @Test
        @DisplayName("권한 없는 사용자의 게시글 삭제 실패")
        @WithUserDetails(
            value = "other",
            setupBefore = TestExecutionEvent.TEST_EXECUTION
        )
        void given_UnauthorizedUser_when_DeletePost_then_ReturnForbidden() {
            performDelete(
                mockMvc,
                BASE_URL + '/' + postId,
                status().isForbidden(),
                jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()),
                jsonPath("$.title").value(HttpStatus.FORBIDDEN.name())
            );
        }
    }
}
