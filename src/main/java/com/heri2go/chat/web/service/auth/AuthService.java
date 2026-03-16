package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.domain.token.RefreshHash;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.response.LoginResponse;
import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import com.heri2go.chat.web.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshHashService refreshHashService;

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
        String username = loginRequest.username();
        log.debug("Attempting login for user: {}", username);

        return userService.getByUsername(username)
                .filter(userResponse -> passwordEncoder.matches(loginRequest.password(), userResponse.password()))
                .onErrorResume(UserNotFoundException -> Mono.error(new BadCredentialsException("Invalid username or password.")))
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid username or password.")))
                .flatMap(userResponse -> {
                    String refreshToken = jwtService.generateRefreshToken(username);
                    return refreshHashService.save(
                            RefreshHash.builder()
                                    .username(username)
                                    .refreshToken(refreshToken)
                                    .build()
                            )
                            .map(refreshHash -> {
                                String accessToken = jwtService.generateAccessToken(username);
                                return LoginResponse.from(userResponse, accessToken, refreshHash.refreshToken());
                            });
                });
    }
}