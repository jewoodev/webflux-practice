package com.heri2go.chat.web.service.auth.response;

public record RefreshResponse(
        String accessToken,
        String refreshToken
) {
    public static RefreshResponse from(String accessToken, String refreshToken) {
        return new RefreshResponse(accessToken, refreshToken);
    }
}
