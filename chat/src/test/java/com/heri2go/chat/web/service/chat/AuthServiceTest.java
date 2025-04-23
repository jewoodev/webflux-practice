package com.heri2go.chat.web.service.chat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserRepository;
import com.heri2go.chat.web.controller.auth.request.UserLoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.JwtService;
import com.heri2go.chat.web.service.auth.response.UserResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private UserRegisterRequest validRegisterRequest;
    private UserLoginRequest validLoginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterRequest = UserRegisterRequest.builder()
                                            .username("testuser")
                                            .password("password123")
                                            .email("test@example.com")
                                            .role("LAB")
                                            .build();

        validLoginRequest = new UserLoginRequest(
                        "testuser",
                        "password123");

        savedUser = User.builder()
                        .username("testuser")
                        .password("encodedPassword")
                        .email("test@example.com")
                        .role(Role.LAB)
                        .build();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디가 아닌 것이 확인되면 정상적으로 회원가입에 성공한다.")
    @Test
    void register_WhenUsernameNotExists_ShouldSaveUser() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        // When
        Mono<UserResponse> result = authService.register(validRegisterRequest);

        // Then
        StepVerifier.create(result)
                    .expectNextMatches(user -> user.username().equals(validRegisterRequest.username()) &&
                                    user.email().equals(validRegisterRequest.email()) &&
                                    user.role().equals("LAB"))
                    .verifyComplete();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디인 경우 예외가 발생한다.")
    @Test
    void register_WhenUsernameExists_ShouldThrowError() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(savedUser));

        // When
        Mono<UserResponse> result = authService.register(validRegisterRequest);

        // Then
        StepVerifier.create(result)
                    .expectError(DuplicatedUsernameException.class)
                    .verify();
    }

    @DisplayName("로그인 시 유저가 존재하고 비밀번호가 일치하면 정상적으로 로그인에 성공한다.")
    @Test
    void login_WithValidCredentials_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(savedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("generatedToken");

        // When
        Mono<UserResponse> result = authService.login(validLoginRequest);

        // Then
        StepVerifier.create(result)
                        .expectNextMatches(user -> user.username().equals(validLoginRequest.username()) &&
                                        user.token().equals("generatedToken"))
                        .verifyComplete();
    }

    @DisplayName("로그인 시 유저가 존재하고 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void login_WithInvalidPassword_ShouldThrowError() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.just(savedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When
        Mono<UserResponse> result = authService.login(validLoginRequest);

        // Then
        StepVerifier.create(result)
                        .expectError(BadCredentialsException.class)
                        .verify();
    }

    @DisplayName("로그인 시 유저가 존재하지 않으면 예외가 발생한다.")
    @Test
    void login_WithNonExistentUsername_ShouldThrowError() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Mono.empty());

        // When
        Mono<UserResponse> result = authService.login(validLoginRequest);

        // Then
        StepVerifier.create(result)
                        .expectError(BadCredentialsException.class)
                        .verify();
    }
}