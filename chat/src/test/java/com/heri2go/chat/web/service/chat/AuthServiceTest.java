package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

import static com.heri2go.chat.domain.user.Role.LAB;

class AuthServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class)
                .then(mongoTemplate.createCollection(User.class))
                .then(redisDao.delete("UserResp::" + testUsername))
                .then(redisDao.delete("RefreshHash:" + testUsername))
                .block();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디가 아닌 것이 확인되면 정상적으로 회원가입에 성공한다.")
    @Test
    void register_WhenUsernameNotExists_ShouldSaveUser() {
        // Given
        String password = "Test password";
        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(password)
                .email("test@example.com")
                .role(LAB)
                .build();

        // When // Then
        StepVerifier.create(authService.register(validRegisterRequest))
                    .expectNextMatches(user -> user.username().equals(validRegisterRequest.username()))
                    .verifyComplete();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디로 요청할 경우 예외가 발생한다.")
    @Test
    void register_WhenUsernameExists_ShouldThrowError() {
        // Given
        String password = "Test password";
        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(password)
                .email("test@example.com")
                .role(LAB)
                .build();

        // When
        StepVerifier.create(userRepository.save(User.from(validRegisterRequest))
                        // Then
                        .then(authService.register(validRegisterRequest))
                )
                    .expectError(DuplicatedUsernameException.class)
                    .verify();
    }

    @DisplayName("로그인 시 유저가 존재하고 비밀번호가 일치하면 정상적으로 로그인에 성공한다.")
    @Test
    void login_WithValidCredentials_ShouldReturnUser() {
        // Given
        String password = "Test password";
        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(password)
                .email("test@example.com")
                .role(LAB)
                .build();

        LoginRequest validLoginRequest = new LoginRequest(testUsername, password);

        // When
        StepVerifier.create(authService.register(validRegisterRequest)
                        // Then
                        .then(authService.login(validLoginRequest))
                )
                        .expectNextMatches(user -> user.username().equals(validLoginRequest.username()))
                        .verifyComplete();
    }

    @DisplayName("로그인 시 유저가 존재하고 비밀번호가 일치하지 않으면 예외가 발생한다.")
    @Test
    void login_WithInvalidPassword_ShouldThrowError() {
        // Given
        String password = "Test password";
        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(password)
                .email("test@example.com")
                .role(LAB)
                .build();

        LoginRequest validLoginRequest = new LoginRequest(testUsername, password);

        LoginRequest invalidLoginRequest = new LoginRequest(testUsername, "Wrong password");

        // When
        StepVerifier.create(userRepository.save(User.from(validRegisterRequest))
                        // Then
                        .then(authService.login(invalidLoginRequest))
                )
                        .expectError(BadCredentialsException.class)
                        .verify();
    }

    @DisplayName("로그인 시 유저가 존재하지 않으면 예외가 발생한다.")
    @Test
    void login_WithNonExistentUsername_ShouldThrowError() {
        // Given
        String password = "Test password";
        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(password)
                .email("test@example.com")
                .role(LAB)
                .build(); // 해당 회원가입 요청이 되지 않은 상태에서

        LoginRequest validLoginRequest = new LoginRequest(testUsername, password); // 로그인 요청이 날아간다.

        // When // Then
        StepVerifier.create(authService.login(validLoginRequest))
                        .expectError(BadCredentialsException.class)
                        .verify();
    }
}