package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chat.UnreadChat;
import com.heri2go.chat.domain.chat.UnreadChatRepository;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;


class UnreadChatServiceTest extends MockTestSupport {

    @Mock
    UnreadChatRepository unreadChatRepository;

    @InjectMocks
    UnreadChatService unreadChatService;

    @DisplayName("사용자는 인증 정보를 통해 자신에게 수신된, 하지만 읽지 않은 메세지에 대한 정보를 조회할 수 있다.")
    @Test
    void userCanGetUnreadChatsInfo() {
        // given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                User.builder()
                        .username("test")
                        .role(Role.LAB)
                        .build());

        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId("Test Chat Id")
                .unreadUsername("Test Unread Username")
                .sender("Test Sender")
                .build();

        // when
        when(unreadChatRepository.findByUnreadUsername(userDetails.getUsername()))
                .thenReturn(Flux.just(testUnreadChat));

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