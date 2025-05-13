package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.user.response.UserResponse;
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

import static com.heri2go.chat.domain.user.Role.LAB;
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

    @DisplayName("유저는 초대된 채팅방의 채팅만 조회할 수 있다.")
    @Test
    void userCanGetChatWhenUserIsInvitedToChatRoom() {
        // given
        String testUsername = "Lab staff 2";

        UserDetailsImpl userDetails = new UserDetailsImpl(
                UserResponse.builder()
                        .username(testUsername)
                        .role(LAB)
                        .build());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Chat testChat = Chat.builder()
                        .content("Test chat content")
                        .sender("Test chat sender")
                        .unreadUsernames(Set.of("Lab chief", "Lab staff 1", "Lab staff 2", "Dentist"))
                        .roomId("Test room id")
                        .lang("Test chat lang")
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
                    .uri("/api/chat/{roomId}", testChat.getRoomId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(ChatResponse.class)
                    .hasSize(1);
    }
}