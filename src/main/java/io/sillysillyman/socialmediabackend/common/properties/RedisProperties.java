package io.sillysillyman.socialmediabackend.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private final RefreshTokenProperties refreshToken = new RefreshTokenProperties();

    @Getter
    @Setter
    public static class RefreshTokenProperties {

        private String prefix;
        private long expiration;
    }
}
