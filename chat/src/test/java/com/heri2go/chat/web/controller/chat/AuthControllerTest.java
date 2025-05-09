package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.web.controller.auth.AuthController;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.auth.AuthService;
import com.heri2go.chat.web.service.auth.response.LoginResponse;
import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import com.heri2go.chat.web.service.user.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static com.heri2go.chat.domain.user.Role.LAB;
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
        String testUsername = "Test username";
        UserRegisterRequest request = UserRegisterRequest.builder()
                        .username(testUsername)
                        .password("Test password")
                        .email("test@example.com")
                        .role("LAB")
                        .build();

        // 유효한 request로 회원가입 요청을 하면
        when(authService.register(request))
                        // 회원가입에 성공하고 testUsername을 갖는 response를 반환한다.
                        .thenReturn(Mono.just(new UserRegisterResponse(testUsername)));


        // When & Then
        webTestClient.post()
                    .uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(request.username());
    }

    @DisplayName("회원가입 시 유효하지 않은 데이터를 입력하면 예외가 발생한다.")
    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() {
        // Given
        UserRegisterRequest invalidRequest = UserRegisterRequest.builder()
                        .username("")
                        .password("123")
                        .email("invalid-email")
                        .role("LAB")
                        .build();

        // When & Then
        webTestClient.post()
                        .uri("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(invalidRequest)
                        .exchange()
                        .expectStatus().isBadRequest();
    }

    @DisplayName("로그인 시 유효한 데이터를 입력하면 정상적으로 로그인에 성공한다.")
    @Test
    void login_WithValidCredentials_ShouldReturnOk() {
        // Given
        LoginRequest validRequest = new LoginRequest("Test username", "Test password");

        LoginResponse loginResponse = LoginResponse.from(UserResponse.builder()
                .username(validRequest.username())
                .role(LAB)
                .build(), "Test token");

        // validRequest로 로그인 시도를 하면
        when(authService.login(validRequest))
                        // 정상적으로 response를 반환한다.
                        .thenReturn(Mono.just(loginResponse));

        // When & Then
        webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(validRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.username").isEqualTo(validRequest.username())
                    .jsonPath("$.role").isEqualTo("LAB");
    }

    @DisplayName("로그인 시 유효하지 않은 데이터를 입력하면 예외가 발생한다.")
    @Test
    void login_WithInvalidCredentials_ShouldReturnBadRequest() {
        // Given
        LoginRequest invalidRequest = new LoginRequest("Test username", "Wrong password");

        when(authService.login(invalidRequest))
                        .thenReturn(Mono.error(new BadCredentialsException("Invalid credentials")));

        // When & Then
        webTestClient.post()
                    .uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
    }
}