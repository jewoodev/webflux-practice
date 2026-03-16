package com.heri2go.chat.domain.user;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;
    private String email;

    @Enumerated(EnumType.STRING)
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
                .role(req.role())
                .build();
    }
}
