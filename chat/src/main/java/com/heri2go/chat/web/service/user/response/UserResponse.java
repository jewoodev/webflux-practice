package com.heri2go.chat.web.service.user.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import lombok.Builder;

@Builder
public record UserResponse(
        @JsonIgnore String id,
        String username,
        @JsonIgnore String password,
        String email,
        Role role
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .role(user.getRole())
                .build();
    }
}
