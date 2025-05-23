package com.heri2go.chat.web.service.auth;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.token.RefreshHash;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.web.controller.auth.request.RefreshRequest;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.heri2go.chat.domain.user.Role.LAB;

class RefreshHashServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        redisDao.delete("RefreshHash:" + testUsername)
                .then(redisDao.delete("UserResp::" + testUsername))
                .then(mongoTemplate.dropCollection(User.class))
                .then(mongoTemplate.createCollection(User.class))
                .block();
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

        // when && then
        refreshHashService.save(refreshHash)
                .block();

        StepVerifier.create(refreshHashRepository.findByUsername(testUsername))
                .expectNextMatches(token -> token.equals(refreshToken))
                .verifyComplete();
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

        authService.register(registerRequest) // 회원가입이 테스트 타겟이 아니므로 비밀번호 암호화 과정은 건너뛴다.
                .block();

        String testRefreshToken = "Test refresh token";
        RefreshHash refreshHash = RefreshHash.builder()
                .username(testUsername)
                .refreshToken(testRefreshToken)
                .build();

        refreshHashService.save(refreshHash)
                .block();

        RefreshRequest refreshRequest = new RefreshRequest(testUsername, testRefreshToken);

        // when && then
        StepVerifier.create(refreshHashService.refresh(refreshRequest))
                .expectNextMatches(refreshResponse -> !refreshResponse.refreshToken().equals(testRefreshToken))
                .verifyComplete();
    }
}