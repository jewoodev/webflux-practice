package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.request.LoginRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.exception.DuplicatedUsernameException;
import com.heri2go.chat.web.service.auth.response.UserRegisterResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        redisDao.delete("UserResp::" + testUsername);
        redisDao.delete("RefreshHash:" + testUsername);
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

        // When
        UserRegisterResponse response = authService.register(validRegisterRequest);

        // Then
        assertThat(response.username()).isEqualTo(validRegisterRequest.username());
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

        userRepository.save(User.from(validRegisterRequest));

        // When // Then
        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(DuplicatedUsernameException.class);
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

        authService.register(validRegisterRequest);

        // When
        var loginResponse = authService.login(validLoginRequest);

        // Then
        assertThat(loginResponse.username()).isEqualTo(validLoginRequest.username());
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

        userRepository.save(User.from(validRegisterRequest));

        LoginRequest invalidLoginRequest = new LoginRequest(testUsername, "Wrong password");

        // When // Then
        assertThatThrownBy(() -> authService.login(invalidLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @DisplayName("로그인 시 유저가 존재하지 않으면 예외가 발생한다.")
    @Test
    void login_WithNonExistentUsername_ShouldThrowError() {
        // Given
        LoginRequest validLoginRequest = new LoginRequest(testUsername, "Test password");

        // When // Then
        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }
}
