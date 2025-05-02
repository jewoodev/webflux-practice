package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ChatControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    void setUp() {
            webTestClient = WebTestClient.bindToController(chatController).build();
    }

    @DisplayName("유저는 초대된 채팅방에만 접속할 수 있다.")
    @Test
    void canGetChatWhenUserIsInvitedToChatRoom() {
        // given
        String testUsername = "Lab staff 2";

        UserDetailsImpl testUserDetails = new UserDetailsImpl(
                User.builder()
                        .username(testUsername)
                        .role(Role.LAB)
                        .build());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUserDetails, null, testUserDetails.getAuthorities());

        // 전제되는 채팅방
        // ChatRoom testChatRoom = ChatRoom.builder()
        //                 .roomName("Test room name")
        //                 .orderId("Test order id")
        //                 .participantIds(Set.of("Lab chief", "Lab staff 1", "Lab staff 2", "Dentist"))
        //                 .lastMessage("last message")
        //                 .lastSender("last sender")
        //                 .lastMessageTime(NOW.minusHours(1))
        //                 .build();

        Chat testChat = Chat.builder()
                        .content("Test content")
                        .sender("Test sender")
                        .unreadUsernames(Set.of("Lab chief", "Lab staff 1", "Lab staff 2", "Dentist"))
                        .roomId("Test_room_id")
                        .lang("Test lang")
                        .sentimentScore(0.0)
                        .createdAt(NOW)
                        .build();

        // when
        String encodedRoomId = URLEncoder.encode(testChat.getRoomId(), StandardCharsets.UTF_8);

        when(chatService.getByRoomIdToInvited(eq(testChat.getRoomId()), any(UserDetailsImpl.class)))
                        .thenReturn(Flux.just(ChatResponse.from(testChat)));

        // then
        webTestClient.mutate()
                    .filter((request, next) -> next.exchange(request)
                                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                    .build()
                    .get()
                    .uri("/api/chat/{roomName}", encodedRoomId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ChatResponse.class)
                    .hasSize(1);
    }
}