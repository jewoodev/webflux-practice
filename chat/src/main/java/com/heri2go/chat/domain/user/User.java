package com.heri2go.chat.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;
    private String email;
    private Role role;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private User(String username, String password, String email, Role role, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User from(UserRegisterRequest req) {
        return User.builder()
                .username(req.username())
                .password(req.password())
                .email(req.email())
                .role(req.role())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}