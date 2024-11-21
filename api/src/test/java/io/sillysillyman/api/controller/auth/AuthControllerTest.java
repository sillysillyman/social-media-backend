package io.sillysillyman.api.controller.auth;

import static io.sillysillyman.api.util.MockMvcTestUtil.performPost;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import io.sillysillyman.api.util.MockMvcTestUtil;
import io.sillysillyman.core.auth.constants.JwtConstants;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String BASE_URL = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManagerFactory emf;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private EntityManager em;

    private void withTransaction(Consumer<EntityManager> block) {
        em.getTransaction().begin();
        block.accept(em);
        em.getTransaction().commit();
    }

    @BeforeEach
    void setUp() {
        em = emf.createEntityManager();

        assert em.createQuery("SELECT COUNT(u) FROM UserEntity u", Long.class)
            .getSingleResult() == 0;

        withTransaction(em -> {
            UserEntity user = UserEntity.builder()
                .username("tester")
                .password(passwordEncoder.encode("password1!"))
                .role(UserRole.USER)
                .build();
            em.persist(user);
        });
    }

    @AfterEach
    void tearDown() {
        withTransaction(em -> {
            em.createQuery("DELETE FROM UserEntity").executeUpdate();

            em.createNativeQuery("ALTER TABLE users ALTER COLUMN id RESTART WITH 1")
                .executeUpdate();
        });
        em.close();
    }

    @Nested
    @DisplayName("로그인 API")
    class Login {

        private static final String REQUEST_BODY = """
            {
                "username": "%s",
                "password": "%s"
            }
            """;

        @Test
        @DisplayName("로그인 성공")
        void given_ValidCredentials_when_Login_then_ReturnTokens() {
            performPost(
                mockMvc,
                BASE_URL + "/login",
                REQUEST_BODY.formatted("tester", "password1!"),
                status().isOk(),
                jsonPath("$.data.accessToken").exists(),
                jsonPath("$.data.refreshToken").exists()
            );
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void given_InvalidPassword_when_Login_then_ReturnUnauthorized() {
            performPost(
                mockMvc,
                BASE_URL + "/login",
                REQUEST_BODY.formatted("tester", "wrongpassword"),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 실패")
        void given_NonExistentUser_when_Login_then_ReturnUnauthorized() {
            performPost(
                mockMvc,
                BASE_URL + "/login",
                REQUEST_BODY.formatted("nonexistent", "password1!"),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {

        @Test
        @DisplayName("로그아웃 성공")
        @WithUserDetails(value = "tester", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void given_AuthenticatedUser_when_Logout_then_ReturnNoContent() {
            performPost(
                mockMvc,
                BASE_URL + "/logout",
                status().isNoContent()
            );
        }

        @Test
        @DisplayName("인증되지 않은 사용자 로그아웃 실패")
        void given_UnauthenticatedUser_when_Logout_then_ReturnUnauthorized() {
            performPost(
                mockMvc,
                BASE_URL + "/logout",
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }

    @Nested
    @DisplayName("토큰 갱신 API")
    class Refresh {

        private static final String REQUEST_BODY = """
            {
                "username": "%s",
                "password": "%s"
            }
            """;
        private String refreshToken;

        @BeforeEach
        void setUp() {
            try {
                MvcResult result = performPost(
                    mockMvc,
                    BASE_URL + "/login",
                    REQUEST_BODY.formatted("tester", "password1!"),
                    status().isOk()
                );

                refreshToken = JsonPath.read(
                    result.getResponse().getContentAsString(),
                    "$.data.refreshToken"
                );
            } catch (Exception e) {
                throw new RuntimeException("토큰 추출 실패", e);
            }
        }

        @Test
        @DisplayName("토큰 갱신 성공")
        void given_ValidRefreshToken_when_Refresh_then_ReturnNewTokens() {
            performPost(
                mockMvc,
                BASE_URL + "/refresh",
                (MockHttpServletRequestBuilder request) -> request.header(
                    JwtConstants.REFRESH_HEADER,
                    refreshToken
                ),
                status().isOk(),
                jsonPath("$.data.accessToken").exists(),
                jsonPath("$.data.refreshToken").exists()
            );
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 실패")
        void given_InvalidRefreshToken_when_Refresh_then_ReturnUnauthorized() {
            MockMvcTestUtil.performPost(
                mockMvc,
                BASE_URL + "/refresh",
                (MockHttpServletRequestBuilder request) -> request.header(
                    JwtConstants.REFRESH_HEADER,
                    "invalid.refresh.token"
                ),
                status().isUnauthorized(),
                jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()),
                jsonPath("$.title").value(HttpStatus.UNAUTHORIZED.name())
            );
        }
    }
}
