package com.heri2go.chat.web.controller.chatroom;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
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

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatRoomControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;
    private static final String TEST_USERNAME = "testuser";

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatRoomController chatRoomController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(chatRoomController)
                .configureClient()
                .build();
    }

    @DisplayName("각 사용자는 자신이 참여 중인 채팅방에 대한 정보들을 조회할 수 있다.")
    @Test
    void userCanGetChatRoomInfoThroughAuthentication() {
        // given
        UserDetailsImpl testUser = new UserDetailsImpl(
                User.builder()
                        .username(TEST_USERNAME)
                        .role(Role.LAB)
                        .build());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USERNAME, null, testUser.getAuthorities());

        // when
        when(chatRoomService.getOwnChatRoomResponse(any(UserDetailsImpl.class)))
                .thenReturn(
                    Flux.just(
                        ChatRoomResponse.builder()
                                    .roomName("test chat room")
                                    .participantIds(Set.of(TEST_USERNAME, "user2"))
                                    .lastMessage("last message")
                                    .lastSender("last sender")
                                    .lastMessageTime(LocalDateTime.now().minusHours(1))
                                    .build()
                    )
                );

        // then
        webTestClient.mutate()
                .filter((request, next) -> next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .get()
                .uri("/api/chat-room/info")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ChatRoomResponse.class)
                .hasSize(1);
    }
}