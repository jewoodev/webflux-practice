package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.IntegrationTestSupport;
import com.heri2go.chat.domain.chat.UnreadChat;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.auth.request.UserRegisterRequest;
import com.heri2go.chat.web.service.chat.response.UnreadChatResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.assertj.core.api.Assertions.assertThat;

class UnreadChatServiceTest extends IntegrationTestSupport {

    private final String testUsername = "Test username";

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        unreadChatRepository.deleteAll();
        redisDao.delete("UserResp::" + testUsername);
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

        authService.register(validRegisterRequest);
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(testUsername);

        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId(1L)
                .unreadUsername(testUsername)
                .sender("Test chat sender")
                .content("Test chat content")
                .build();

        unreadChatRepository.save(testUnreadChat);

        // when
        List<UnreadChatResponse> responses = unreadChatService.getOwnByUserDetails(userDetails);

        // then
        assertThat(responses).hasSize(1);
        UnreadChatResponse response = responses.get(0);
        assertThat(response.chatId()).isEqualTo(testUnreadChat.getChatId());
        assertThat(response.unreadUsername()).isEqualTo(testUnreadChat.getUnreadUsername());
        assertThat(response.sender()).isEqualTo(testUnreadChat.getSender());
    }
}
