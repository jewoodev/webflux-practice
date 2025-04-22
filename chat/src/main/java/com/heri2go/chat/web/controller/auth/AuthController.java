package com.heri2go.chat.web.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.heri2go.chat.web.controller.auth.request.UserLoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.response.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody UserRegisterRequest registerRequest) {
        return authService.register(registerRequest)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Registration error: {}", e.getMessage()))
                .onErrorResume(e -> {
                    if (e instanceof DuplicatedUsernameException) {
                        return Mono.just(ResponseEntity.badRequest().body(null));
                    }
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponse>> login(@Valid @RequestBody UserLoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Login error: {}", e.getMessage()))
                .onErrorResume(e -> {
                    if (e instanceof BadCredentialsException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}