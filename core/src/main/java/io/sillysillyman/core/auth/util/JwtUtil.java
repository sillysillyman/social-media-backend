package io.sillysillyman.core.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.sillysillyman.core.auth.properties.JwtProperties;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j(topic = "JwtUtil")
@Component
public class JwtUtil {

    private static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    private final JwtProperties jwtProperties;
    private final Key key;

    public String generateAccessToken(
        String username,
        Collection<? extends GrantedAuthority> authorities
    ) {
        return BEARER_PREFIX +
            generateToken(username, authorities, jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(
        String username,
        Collection<? extends GrantedAuthority> authorities
    ) {
        return generateToken(username, authorities, jwtProperties.getRefreshTokenExpiration());
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = getClaims(token);

        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get(AUTHORIZATION_KEY, List.class);

        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    public String resolveToken(String token) {
        return token.startsWith(BEARER_PREFIX) ? token.substring(BEARER_PREFIX.length()) : token;
    }

    public boolean isTokenValid(String token) {
        if (token == null) {
            log.error("Jwt token is null");
            return false;
        }

        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token: The token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: The token format is not supported", e);
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: The token format is invalid", e);
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: The token signature is invalid", e);
        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT token: The token is either null or empty", e);
        }
        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(resolveToken(token))
            .getBody();
    }

    private String generateToken(
        String username,
        Collection<? extends GrantedAuthority> authorities,
        long expiration
    ) {
        Date now = new Date();
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(
            AUTHORIZATION_KEY,
            authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        );

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expiration))
            .signWith(key, SIGNATURE_ALGORITHM)
            .compact();
    }
}
