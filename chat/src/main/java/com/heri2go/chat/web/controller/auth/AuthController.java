package com.heri2go.chat.web.controller.auth;

import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.RefreshRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.RefreshHashService;
import com.heri2go.chat.web.service.auth.response.LoginResponse;
import com.heri2go.chat.web.service.auth.response.RefreshResponse;
import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final RefreshHashService refreshHashService;

    @PostMapping("/register") // 회원가입
    public Mono<ResponseEntity<UserRegisterResponse>> register(@Valid @RequestBody UserRegisterRequest registerRequest) {
        return authService.register(registerRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login") // 로그인
    public Mono<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refresh") // 토큰 리프레쉬
    public Mono<ResponseEntity<RefreshResponse>> refreshToken(@Valid @RequestBody RefreshRequest refreshRequest) {
        return refreshHashService.refresh(refreshRequest)
                .map(ResponseEntity::ok);
    }
}