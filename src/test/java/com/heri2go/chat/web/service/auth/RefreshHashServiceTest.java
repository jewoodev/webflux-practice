package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.token.RefreshHash;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.request.RefreshRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.auth.response.RefreshResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshHashServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        redisDao.delete("RefreshHash:" + testUsername);
        redisDao.delete("UserResp::" + testUsername);
        userRepository.deleteAll();
    }

    @DisplayName("리프레쉬 토큰은 메모리에 저장될 수 있고, 저장된 토큰은 읽어질 수 있다.")
    @Test
    void refreshToken_canBeStored_andBeReferred() {
        // given
        String refreshToken = "Test refresh token";
        RefreshHash refreshHash = RefreshHash.builder()
                .username(testUsername)
                .refreshToken(refreshToken)
                .build();

        // when
        refreshHashService.save(refreshHash);

        // then
        String storedToken = refreshHashRepository.findByUsername(testUsername);
        assertThat(storedToken).isEqualTo(refreshToken);
    }

    @DisplayName("리프레쉬 요청을 통해 액세스 토큰과 리프레쉬 토큰은 갱신될 수 있다.")
    @Test
    void tokensCanBeRenewed_byRefreshRequest() {
        // given
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .username("Test username")
                .password("Test password")
                .email("Test email")
                .role(LAB)
                .build();

        authService.register(registerRequest);

        String testRefreshToken = "Test refresh token";
        RefreshHash refreshHash = RefreshHash.builder()
                .username(testUsername)
                .refreshToken(testRefreshToken)
                .build();

        refreshHashService.save(refreshHash);

        RefreshRequest refreshRequest = new RefreshRequest(testUsername, testRefreshToken);

        // when
        RefreshResponse refreshResponse = refreshHashService.refresh(refreshRequest);

        // then
        assertThat(refreshResponse.refreshToken()).isNotEqualTo(testRefreshToken);
    }
}
