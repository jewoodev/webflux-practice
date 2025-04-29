package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.AuthController;
import com.heri2go.chat.web.controller.auth.request.UserLoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
            webTestClient = WebTestClient.bindToController(authController).build();
    }

    @DisplayName("회원가입 시 유효한 데이터를 입력하면 정상적으로 회원가입에 성공한다.")
    @Test
    void register_WithValidData_ShouldReturnOk() {
            // Given
            UserRegisterRequest registerDto = UserRegisterRequest.builder()
                            .username("testuser")
                            .password("password123")
                            .email("test@example.com")
                            .role("LAB")
                            .build();

            User savedUser = User.builder()
                            .username(registerDto.username())
                            .email(registerDto.email())
                            .role(Role.valueOf(registerDto.role()))
                            .build();

            when(authService.register(any(UserRegisterRequest.class)))
                            .thenReturn(Mono.just(UserResponse.from(savedUser, null)));


            // When & Then
            webTestClient.post()
                        .uri("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(registerDto)
                        .exchange()
                        .expectStatus().isOk()
                        .expectBody()
                        .jsonPath("$.username").isEqualTo(registerDto.username())
                        .jsonPath("$.email").isEqualTo(registerDto.email())
                        .jsonPath("$.role").isEqualTo("LAB");
    }

    @DisplayName("회원가입 시 유효하지 않은 데이터를 입력하면 예외가 발생한다.")
    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() {
            // Given
            UserRegisterRequest invalidDto = UserRegisterRequest.builder()
                            .username("")
                            .password("123")
                            .email("invalid-email")
                            .role("LAB")
                            .build();

            // When & Then
            webTestClient.post()
                            .uri("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(invalidDto)
                            .exchange()
                            .expectStatus().isBadRequest();
    }

    @DisplayName("로그인 시 유효한 데이터를 입력하면 정상적으로 로그인에 성공한다.")
    @Test
    void login_WithValidCredentials_ShouldReturnOk() {
        // Given
        UserLoginRequest loginRequest = new UserLoginRequest("testuser",
                                                        "password123");

        UserResponse userResp = UserResponse.from(User.builder()
                        .username(loginRequest.username())
                        .role(Role.LAB)
                        .build(), null);

        when(authService.login(any(UserLoginRequest.class)))
                        .thenReturn(Mono.just(userResp));

        // When & Then
        webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(loginRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(loginRequest.username())
                    .jsonPath("$.role").isEqualTo("LAB");
    }

    @DisplayName("로그인 시 유효하지 않은 데이터를 입력하면 예외가 발생한다.")
    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() {
        // Given
        UserLoginRequest invalidReq = new UserLoginRequest("testuser",
                                                            "wrongpassword");

        when(authService.login(any(UserLoginRequest.class)))
                        .thenReturn(Mono.error(new BadCredentialsException("Invalid credentials")));

        // When & Then
        webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidReq)
                    .exchange()
                    .expectStatus().isBadRequest();
    }
}