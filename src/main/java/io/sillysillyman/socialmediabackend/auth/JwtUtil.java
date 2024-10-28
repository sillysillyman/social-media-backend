package io.sillysillyman.socialmediabackend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.sillysillyman.socialmediabackend.domain.user.UserRole;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    private final JwtProperties jwtProperties;
    private final Key key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());

    public String generateAccessToken(String username, UserRole role) {
        return BEARER_PREFIX + generateToken(username, role,
            jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(String username, UserRole role) {
        return generateToken(username, role, jwtProperties.getRefreshTokenExpiration());
    }

    private String generateToken(String username, UserRole role, long expiration) {
        Date now = new Date();

        return Jwts.builder()
            .setSubject(username)
            .claim(AUTHORIZATION_KEY, role)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expiration))
            .signWith(key, SIGNATURE_ALGORITHM)
            .compact();
    }
}
