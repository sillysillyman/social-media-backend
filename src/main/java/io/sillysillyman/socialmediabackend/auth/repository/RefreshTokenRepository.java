package io.sillysillyman.socialmediabackend.auth.repository;

import io.sillysillyman.socialmediabackend.common.properties.RedisProperties;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j(topic = "RefreshTokenRepository")
@Component
public class RefreshTokenRepository {

    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public void save(String username, String refreshToken) {
        stringRedisTemplate.opsForValue().set(
            generateKey(username),
            refreshToken,
            Duration.ofMillis(redisProperties.getRefreshToken().getExpiration())
        );
        log.debug("Saved refresh token for user: {}", username);
    }

    public Optional<String> findByUsername(String username) {
        String key = generateKey(username);
        String token = stringRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    public void deleteByUsername(String username) {
        String key = generateKey(username);
        stringRedisTemplate.delete(key);
        log.debug("Deleted refresh token for user: {}", username);
    }

    private String generateKey(String username) {
        return redisProperties.getRefreshToken().getPrefix() + username;
    }
}
