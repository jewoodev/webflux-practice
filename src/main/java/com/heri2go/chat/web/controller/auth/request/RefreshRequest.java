package com.heri2go.chat.web.controller.auth.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "잘못된 리프레시 요청입니다.")
        String username,

        @NotBlank(message = "잘못된 리프레시 요청입니다.")
        String refreshToken
) {
}
