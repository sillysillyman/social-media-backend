package io.sillysillyman.core.auth;

import lombok.Builder;

@Builder
public record Token(String accessToken, String refreshToken) {

    public static Token of(String accessToken, String refreshToken) {
        return Token.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
