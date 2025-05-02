package com.heri2go.chat.web.controller.chatroom;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.user.Role;
import com.heri2go.chat.domain.user.User;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chat.ChatService;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatRoomControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Mock
    private ChatRoomService chatRoomService;

    @Mock 
    private ChatService chatService;

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
        String testUsername = "testuser";

        UserDetailsImpl testUserDetails = new UserDetailsImpl(
                User.builder()
                        .username(testUsername)
                        .role(Role.LAB)
                        .build());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUserDetails, null, testUserDetails.getAuthorities());

        // when
        when(chatRoomService.getOwnChatRoomResponse(any(UserDetailsImpl.class)))
                .thenReturn(
                    Flux.just(
                        ChatRoomResponse.builder()
                                    .roomName("test chat room")
                                    .participantIds(Set.of(testUsername, "user2"))
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
    
    @DisplayName("주문이 생성되면 채팅방이 생성된다.")
    @Test
    void chatRoom_iscreated_when_order_isCreated() {
        // given
        ChatRoomCreateRequest requestedStartedWithOrder = ChatRoomCreateRequest.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(Set.of("Lab chief", "Lab staff 1", "Lab staff 2"))
                .lastMessage("last message")
                .lastSender("last sender")
                .lastMessageTime(NOW.minusHours(1))
                .build();

        ChatRoomResponse response = ChatRoomResponse.builder()
                                                .roomName("Test room name")
                                                .orderId("Test order id")
                                                .participantIds(Set.of("Lab chief", "Lab staff 1", "Lab staff 2", "Dentist"))
                                                .lastMessage("last message")
                                                .lastSender("last sender")
                                                .lastMessageTime(NOW.minusHours(1))
                                                .build();

        // when
        when(chatRoomService.save(requestedStartedWithOrder))
                .thenReturn(Mono.just(response));

        // then
        webTestClient.post()
                .uri("/api/chat-room/create")
                .bodyValue(requestedStartedWithOrder)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ChatRoomResponse.class)
                .isEqualTo(ChatRoomResponse.builder()
                        .roomName("Test room name")
                        .orderId("Test order id")
                        .participantIds(Set.of("Lab chief", "Lab staff 1", "Lab staff 2", "Dentist"))
                        .lastMessage("last message")
                        .lastSender("last sender")
                        .lastMessageTime(NOW.minusHours(1))
                        .build());
    }
}