package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

class AuthServiceTest extends IntegrationTestSupport {

    private UserRegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private LoginRequest invalidLoginRequest;

    @BeforeEach
    void setUp() {
        String username = "Test username";
        String password = "Test password";
        validRegisterRequest = UserRegisterRequest.builder()
                                            .username(username)
                                            .password(password)
                                            .email("test@example.com")
                                            .role("LAB")
                                            .build();

        validLoginRequest = new LoginRequest(username, password);

        invalidLoginRequest = new LoginRequest(username, "Wrong password");
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class)
                .then(mongoTemplate.createCollection(User.class))
                .then(redisDao.delete("user::" + validLoginRequest.username()))
                .block();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디가 아닌 것이 확인되면 정상적으로 회원가입에 성공한다.")
    @Test
    void register_WhenUsernameNotExists_ShouldSaveUser() {
        // Given // When // Then
        StepVerifier.create(authService.register(validRegisterRequest))
                    .expectNextMatches(user -> user.username().equals(validRegisterRequest.username()))
                    .verifyComplete();
    }

    @DisplayName("회원가입 시 유저가 이미 사용중인 아이디로 요청할 경우 예외가 발생한다.")
    @Test
    void register_WhenUsernameExists_ShouldThrowError() {
        // Given // When
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
        // Given // When
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
        // Given // When
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
        // Given // When // Then
        StepVerifier.create(authService.login(validLoginRequest))
                        .expectError(BadCredentialsException.class)
                        .verify();
    }
}