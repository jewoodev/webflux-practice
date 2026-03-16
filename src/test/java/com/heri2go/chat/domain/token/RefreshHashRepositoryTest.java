package com.heri2go.chat.domain.token;

import com.heri2go.chat.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class RefreshHashRepositoryTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        redisDao.delete("RefreshHash:" + testUsername)
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
        refreshHashRepository.save(refreshHash)
                .block();

        StepVerifier.create(refreshHashRepository.findByUsername(testUsername))
                .expectNextMatches(token -> token.equals(refreshToken))
                .verifyComplete();
    }
}