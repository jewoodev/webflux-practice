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
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshHashService refreshHashService;

    public UserRegisterResponse register(UserRegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicatedUsernameException("Username already exists");
        }

        User user = userRepository.save(
                User.from(UserRegisterRequest.withEncodedPassword(
                        registerRequest,
                        passwordEncoder.encode(registerRequest.password()))
                )
        );
        return new UserRegisterResponse(user.getUsername());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.username();
        log.debug("Attempting login for user: {}", username);

        UserResponse userResponse;
        try {
            userResponse = userService.getByUsername(username);
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        if (!passwordEncoder.matches(loginRequest.password(), userResponse.password())) {
            throw new BadCredentialsException("Invalid username or password.");
        }

        String refreshToken = jwtService.generateRefreshToken(username);
        RefreshHash refreshHash = refreshHashService.save(
                RefreshHash.builder()
                        .username(username)
                        .refreshToken(refreshToken)
                        .build()
        );

        String accessToken = jwtService.generateAccessToken(username);
        return LoginResponse.from(userResponse, accessToken, refreshHash.refreshToken());
    }
}
