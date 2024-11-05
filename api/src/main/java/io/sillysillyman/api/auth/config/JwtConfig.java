package io.sillysillyman.api.auth.config;

import io.jsonwebtoken.security.Keys;
import io.sillysillyman.core.auth.properties.JwtProperties;
import java.security.Key;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
@Configuration
public class JwtConfig {

    private final JwtProperties jwtProperties;

    @Bean
    public Key key() {
        byte[] bytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(bytes);
    }
}
