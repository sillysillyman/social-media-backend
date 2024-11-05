package io.sillysillyman.api.controller.auth.dto;

import io.sillysillyman.core.auth.Token;

public record TokenResponse(String accessToken, String refreshToken) {

    public static TokenResponse from(Token token) {
        return new TokenResponse(token.accessToken(), token.refreshToken());
    }
}
