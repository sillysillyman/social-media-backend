package io.sillysillyman.core.auth.repository;

import io.sillysillyman.core.auth.exception.TokenStorageErrorCode;
import io.sillysillyman.core.auth.exception.detail.TokenDeleteFailedException;
import io.sillysillyman.core.auth.exception.detail.TokenRetrieveFailedException;
import io.sillysillyman.core.auth.exception.detail.TokenSaveFailedException;
import io.sillysillyman.core.common.properties.RedisProperties;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j(topic = "RefreshTokenRepository")
@Component
public class RefreshTokenRepository {

    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public void save(String username, String refreshToken) {
        try {
            stringRedisTemplate.opsForValue().set(
                generateKey(username),
                refreshToken,
                Duration.ofMillis(redisProperties.getRefreshToken().getExpiration())
            );
            log.debug("Saved refresh token for user: {}", username);
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to save refresh token for user: {}", username, e);
            throw new TokenSaveFailedException(TokenStorageErrorCode.REFRESH_TOKEN_SAVE_FAILED, e);
        }
    }

    public Optional<String> findByUsername(String username) {
        try {
            String key = generateKey(username);
            String token = stringRedisTemplate.opsForValue().get(key);
            return Optional.ofNullable(token);
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to retrieve refresh token for user: {}", username, e);
            throw new TokenRetrieveFailedException(
                TokenStorageErrorCode.REFRESH_TOKEN_RETRIEVE_FAILED,
                e
            );
        }
    }

    public void deleteByUsername(String username) {
        try {
            String key = generateKey(username);
            stringRedisTemplate.delete(key);
            log.debug("Deleted refresh token for user: {}", username);
        } catch (RedisConnectionFailureException e) {
            log.error("Failed to delete refresh token for user: {}", username, e);
            throw new TokenDeleteFailedException(
                TokenStorageErrorCode.REFRESH_TOKEN_DELETE_FAILED,
                e
            );
        }
    }

    private String generateKey(String username) {
        return redisProperties.getRefreshToken().getPrefix() + username;
    }
}
