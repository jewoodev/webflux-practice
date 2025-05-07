package com.heri2go.chat.web.service.auth.response;

import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record LoginResponse(
        String username,
        Role role,
        String token
) {
    public static LoginResponse from(UserResponse userResponse, String token) {
        return LoginResponse.builder()
                .username(userResponse.username())
                .role(userResponse.role())
                .token(token)
                .build();
    }
}
