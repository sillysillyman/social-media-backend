package io.sillysillyman.core.common.constants;

import static io.sillysillyman.core.domain.user.UserRole.USER;

import io.sillysillyman.core.domain.user.UserRole;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class TestConstants {

    public static final Long USER_ID = 1L;
    public static final Long POST_ID = 1L;
    public static final Long COMMENT_ID = 1L;
    public static final Long REPLY_ID = 1L;

    public static final Long UNAUTHORIZED_USER_ID = 2L;
    public static final Long ANOTHER_POST_ID = 2L;
    public static final Long ANOTHER_COMMENT_ID = 2L;
    public static final Long ANOTHER_REPLY_ID = 2L;

    public static final Long NON_EXISTENT_ID = 999L;

    public static final String USERNAME = "username";
    public static final String UNAUTHORIZED_USERNAME = "unauthorizedUsername";

    public static final String PASSWORD = "password1!";
    public static final String NEW_PASSWORD = "newPassword1!";
    public static final String INCORRECT_PASSWORD = "incorrectPassword1!";
    public static final String ENCODED_PASSWORD = "encodedPassword1!";
    public static final String ENCODED_NEW_PASSWORD = "encodedNewPassword1!";

    public static final String CONTENT = "content";
    public static final String UPDATED_CONTENT = "updated content";

    public static final int FIRST_PAGE_NUMBER = 0;
    public static final int SECOND_PAGE_NUMBER = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final Instant BASE_TIME = Instant.parse("2024-01-01T00:00:00Z");

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";
    public static final String NEW_ACCESS_TOKEN = "newAccessToken";
    public static final String NEW_REFRESH_TOKEN = "newRefreshToken";
    public static final UserRole USER_ROLE = USER;
    public static final Collection<? extends GrantedAuthority> AUTHORITIES =
        Collections.singletonList(new SimpleGrantedAuthority(USER_ROLE.name()));
}
