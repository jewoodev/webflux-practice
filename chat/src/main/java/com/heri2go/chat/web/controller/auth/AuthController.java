package com.heri2go.chat.web.controller.auth;

import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.response.LoginResponse;
import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserRegisterResponse>> register(@Valid @RequestBody UserRegisterRequest registerRequest) {
        return authService.register(registerRequest)
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Registration error: {}", e.getMessage()));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
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