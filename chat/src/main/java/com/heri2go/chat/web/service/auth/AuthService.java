package com.heri2go.chat.web.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.controller.auth.request.UserLoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.response.UserResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<UserResponse> register(UserRegisterRequest userDto) {
        return userRepository.findByUsername(userDto.username())
                .flatMap(existingUser -> Mono.error(new DuplicatedUsernameException("Username already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    String encodedPassword = passwordEncoder.encode(userDto.password());
                    return Mono.just(
                            User.from(
                                    UserRegisterRequest.withEncodedPassword(userDto, encodedPassword)));
                }))
                .log()
                .cast(User.class)
                .flatMap(user -> {
                    return userRepository.save(user)
                            .map(savedUser -> UserResponse.from(savedUser, null));
                });
    }

    public Mono<UserResponse> login(UserLoginRequest loginDto) {
        log.debug("Attempting login for user: {}", loginDto.username());

        return userRepository.findByUsername(loginDto.username())
                .filter(user -> passwordEncoder.matches(loginDto.password(), user.getPassword()))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password.")))
                .map(user -> {
                    String token = jwtService.generateToken(user.getUsername());
                    return UserResponse.from(user, token);
                });
    }
}