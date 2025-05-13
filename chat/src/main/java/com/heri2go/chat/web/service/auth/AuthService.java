package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import com.heri2go.chat.web.service.user.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.response.LoginResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<UserRegisterResponse> register(UserRegisterRequest registerRequest) {
        return userRepository.existsByUsername(registerRequest.username())
                .flatMap(isExist -> {
                    if (isExist) {
                        return Mono.error(new DuplicatedUsernameException("Username already exists"));
                    }
                    return Mono.just(registerRequest);
                })
                .flatMap(request ->
                        userRepository.save(
                                User.from(UserRegisterRequest.withEncodedPassword(
                                        registerRequest,
                                        passwordEncoder.encode(request.password()))
                                )
                        ).
                        map(user -> new UserRegisterResponse(user.getUsername()))
                );
    }

    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        log.debug("Attempting login for user: {}", loginRequest.username());

        return userService.getByUsername(loginRequest.username())
                .filter(user -> passwordEncoder.matches(loginRequest.password(), user.password()))
                .onErrorResume(UserNotFoundException -> Mono.error(new BadCredentialsException("Invalid username or password.")))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password.")))
                .map(user -> {
                    String token = jwtService.generateToken(user.username());
                    return LoginResponse.from(user, token);
                });
    }
}