package io.sillysillyman.api.controller.user;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class UserControllerTest {

    private static final String BASE_URL = "/api/v1/users";
    private static final Long NON_EXISTENT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private EntityManager em;
    private Long userId;

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
                .password(passwordEncoder.encode("password1!"))
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

    @DisplayName("회원가입 API")
    @Nested
    class Signup {

        private static final String REQUEST_BODY = """
            {
                "username": "%s",
                "password": "%s",
                "confirmPassword": "%s"
            }
            """;

        @DisplayName("회원가입 성공")
        @Test
        void given_ValidSignupRequest_when_Signup_then_ReturnCreated() {
            performPost(
                mockMvc,
                BASE_URL + "/signup",
                REQUEST_BODY.formatted("newtester", "password1!", "password1!"),
                status().isCreated(),
                jsonPath("$.data.userId").exists(),
                jsonPath("$.data.username").value("newtester")
            );
        }

        @DisplayName("중복된 사용자명으로 회원가입 실패")
        @Test
        void given_DuplicateUsername_when_Signup_then_ReturnBadRequest() {
            performPost(
                mockMvc,
                BASE_URL + "/signup",
                REQUEST_BODY.formatted("tester", "password1!", "password1!"),
                status().isConflict(),
                jsonPath("$.status").value(HttpStatus.CONFLICT.value()),
                jsonPath("$.title").value(HttpStatus.CONFLICT.name())
            );
        }

        @DisplayName("비밀번호와 비밀번호 확인 불일치로 회원가입 실패")
        @Test
        void given_PasswordMismatch_when_Signup_then_ReturnBadRequest() {
            performPost(
                mockMvc,
                BASE_URL + "/signup",
                REQUEST_BODY.formatted("newtester", "password1!", "different1!"),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }
    }

    @DisplayName("사용자 조회 API")
    @Nested
    class GetUser {

        @DisplayName("사용자 조회 성공")
        @Test
        void given_ExistingUserId_when_GetUser_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL + "/" + userId,
                status().isOk(),
                jsonPath("$.data.userId").value(userId),
                jsonPath("$.data.username").value("tester")
            );
        }

        @DisplayName("존재하지 않는 사용자 조회 실패")
        @Test
        void given_NonExistentUserId_when_GetUser_then_ReturnNotFound() {
            performGet(
                mockMvc,
                BASE_URL + "/" + NON_EXISTENT_ID,
                status().isNotFound(),
                jsonPath("$.status").value(HttpStatus.NOT_FOUND.value()),
                jsonPath("$.title").value(HttpStatus.NOT_FOUND.name())
            );
        }
    }

    @DisplayName("사용자의 게시물 조회 API")
    @Nested
    class GetUserPosts {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);

                PostEntity post1 = PostEntity.builder()
                    .content("first post")
                    .user(user)
                    .build();
                PostEntity post2 = PostEntity.builder()
                    .content("second post")
                    .user(user)
                    .build();

                em.persist(post1);
                em.persist(post2);
            });
        }

        @DisplayName("사용자의 게시물 목록 조회 성공")
        @Test
        void given_ExistingUserId_when_GetUserPosts_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL + "/" + userId + "/posts",
                status().isOk(),
                jsonPath("$.content").isArray(),
                jsonPath("$.content[0].content").value("second post"),
                jsonPath("$.content[1].content").value("first post")
            );
        }
    }

    @DisplayName("내 게시물 조회 API")
    @Nested
    class GetMyPosts {

        @BeforeEach
        void setUp() {
            withTransaction(em -> {
                UserEntity user = em.find(UserEntity.class, userId);

                PostEntity post1 = PostEntity.builder()
                    .content("my first post")
                    .user(user)
                    .build();
                PostEntity post2 = PostEntity.builder()
                    .content("my second post")
                    .user(user)
                    .build();

                em.persist(post1);
                em.persist(post2);
            });
        }

        @DisplayName("내 게시물 목록 조회 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_AuthenticatedUser_when_GetMyPosts_then_ReturnOkResponse() {
            performGet(
                mockMvc,
                BASE_URL + "/me/posts",
                status().isOk(),
                jsonPath("$.content").isArray(),
                jsonPath("$.content[0].content").value("my second post"),
                jsonPath("$.content[1].content").value("mt first post")
            );
        }

        @DisplayName("인증되지 않은 사용자의 내 게시물 조회 실패")
        @Test
        void given_UnauthenticatedUser_when_GetMyPosts_then_ReturnUnauthorized() {
            performGet(
                mockMvc,
                BASE_URL + "/me/posts",
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }

    @DisplayName("비밀번호 변경 API")
    @Nested
    class ChangePassword {

        private static final String REQUEST_BODY = """
            {
                "currentPassword": "%s",
                "newPassword": "%s",
                "confirmNewPassword": "%s"
            }
            """;

        @DisplayName("비밀번호 변경 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_ValidPasswordChangeRequest_when_ChangePassword_then_ReturnNoContent() {
            performPut(
                mockMvc,
                BASE_URL + "/me/password",
                REQUEST_BODY.formatted("password1!", "newpassword1!", "newpassword1!"),
                status().isNoContent()
            );
        }

        @DisplayName("잘못된 현재 비밀번호로 변경 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_InvalidCurrentPassword_when_ChangePassword_then_ReturnBadRequest() {
            performPut(
                mockMvc,
                BASE_URL + "/me/password",
                REQUEST_BODY.formatted("wrongpassword", "newpassword1!", "newpassword1!"),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }

        @DisplayName("현재 비밀번호와 새 비밀번호가 같을 때 변경 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_SameNewPassword_when_ChangePassword_then_ReturnBadRequest() {
            performPut(
                mockMvc,
                BASE_URL + "/me/password",
                REQUEST_BODY.formatted("password1!", "password1!", "password1!"),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }

        @DisplayName("새 비밀번호와 새 비밀번호 확인 불일치로 변경 실패")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_NewPasswordMismatch_when_ChangePassword_then_ReturnBadRequest() {
            performPut(
                mockMvc,
                BASE_URL + "/me/password",
                REQUEST_BODY.formatted("password1!", "newpassword1!", "different1!"),
                status().isBadRequest(),
                jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()),
                jsonPath("$.title").value(HttpStatus.BAD_REQUEST.name())
            );
        }

        @DisplayName("인증되지 않은 사용자의 비밀번호 변경 실패")
        @Test
        void given_UnauthenticatedUser_when_ChangePassword_then_ReturnUnauthorized() {
            performPut(
                mockMvc,
                BASE_URL + "/me/password",
                REQUEST_BODY.formatted("password1!", "newpassword1!", "newpassword1!"),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }

    @DisplayName("회원 탈퇴 API")
    @Nested
    class Withdraw {

        @DisplayName("회원 탈퇴 성공")
        @Test
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_AuthenticatedUser_when_Withdraw_then_ReturnNoContent() {
            performDelete(
                mockMvc,
                BASE_URL + "/me",
                status().isNoContent()
            );

            // TODO: 소프르 딜리트 사용자 조회 안 되도록 수정
            performGet(
                mockMvc,
                BASE_URL + "/" + userId,
                status().isOk(),
                jsonPath("$.data.userId").value(userId),
                jsonPath("$.data.username").value("tester")
            );
        }

        @DisplayName("인증되지 않은 사용자의 회원 탈퇴 실패")
        @Test
        void given_UnauthenticatedUser_when_Withdraw_then_ReturnUnauthorized() {
            performDelete(
                mockMvc,
                BASE_URL + "/me",
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }
}
