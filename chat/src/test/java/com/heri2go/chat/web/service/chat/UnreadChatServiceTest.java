package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.chat.UnreadChat;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.heri2go.chat.domain.user.Role.LAB;

class UnreadChatServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(User.class)
                .then(mongoTemplate.createCollection(User.class))
                .then(mongoTemplate.dropCollection(UnreadChat.class))
                .then(mongoTemplate.createCollection(UnreadChat.class))
                .then(redisDao.delete("UserResp::" + testUsername))
                .block();
    }

    @DisplayName("유저는 인증 정보를 통해 '자신에게 수신된, 하지만 읽지 않은 메세지'에 대한 정보를 조회할 수 있다.")
    @Test
    void userCanGetUnreadChatsInfo_throughUserDetails() {
        // given
        String testPassword = "Test password";

        UserRegisterRequest validRegisterRequest = UserRegisterRequest.builder()
                .username(testUsername)
                .password(testPassword)
                .email("test@example.com")
                .role(LAB)
                .build();

        UserDetailsImpl userDetails = (UserDetailsImpl) authService.register(validRegisterRequest)
                .then(userDetailsService.findByUsername(testUsername))
                .block();

        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId("Test chat id")
                .unreadUsername(testUsername)
                .sender("Test chat sender")
                .content("Test chat content")
                .build();

        unreadChatRepository.save(testUnreadChat)
                .block();

        // then
        StepVerifier.create(unreadChatService.getOwnByUserDetails(userDetails))
                .expectNextMatches(response ->
                        response.chatId().equals(testUnreadChat.getChatId()) &&
                        response.unreadUsername().equals(testUnreadChat.getUnreadUsername()) &&
                        response.sender().equals(testUnreadChat.getSender())
                )
                .verifyComplete();
    }
}