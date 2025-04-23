package com.heri2go.chat.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String password;
    private String email;
    private Role role;

    @Builder
    private User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static User from(UserRegisterRequest req) {
        return User.builder()
                .username(req.username())
                .password(req.password())
                .email(req.email())
                .role(Role.valueOf(req.role()))
                .build();
    }
}