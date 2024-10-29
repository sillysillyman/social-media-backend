package io.sillysillyman.socialmediabackend.auth.repository;

import io.sillysillyman.socialmediabackend.common.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RefreshTokenRepository {

    private final RedisProperties redisProperties;
    private final StringRedisTemplate stringRedisTemplate;
}
