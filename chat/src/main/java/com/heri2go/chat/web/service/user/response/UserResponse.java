package com.heri2go.chat.web.service.user.response;

import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import lombok.Builder;

@Builder
public record UserResponse(
        String id,
        String username,
        String password,
        String email,
        Role role
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
