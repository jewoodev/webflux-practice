package com.heri2go.chat.web.service.auth.response;

import com.heri2go.chat.domain.user.User;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record UserResponse(
        String username,
        String email,
        String role,
        String token
) {
    public static UserResponse from(User user, String token) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .token(token)
                .build();
    }
}
