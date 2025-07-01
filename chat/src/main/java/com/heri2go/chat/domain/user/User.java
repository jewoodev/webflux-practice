package com.heri2go.chat.domain.user;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

import static org.springframework.util.Assert.state;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class User extends BaseTimeEntity {
    private String id;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;

    private String email;

    private Role role;

    private UserStatus status;

    @Builder
    private User(String username, String passwordHash, String email, Role role, UserStatus status) {
        this.username = Objects.requireNonNull(username);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.email = Objects.requireNonNull(email);
        this.role = Objects.requireNonNull(role);
        this.status = UserStatus.PENDING;
    }

    public static User from(UserRegisterRequest req) {
        return User.builder()
                .username(req.username())
                .passwordHash(req.password())
                .email(req.email())
                .role(req.role())
                .build();
    }

    public void activate() {
        state(status == UserStatus.PENDING, "PENDING 상태가 아닙니다.");

        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        state(status == UserStatus.ACTIVE, "ACTIVE 상태가 아닙니다.");

        this.status = UserStatus.DEACTIVATED;
    }
}