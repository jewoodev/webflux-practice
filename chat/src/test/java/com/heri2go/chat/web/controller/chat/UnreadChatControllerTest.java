package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.UnreadChatService;
import com.heri2go.chat.web.service.chat.response.UnreadChatResponse;
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

import static com.heri2go.chat.domain.user.Role.LAB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UnreadChatControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;

    @Mock
    UnreadChatService unreadChatService;

    @InjectMocks
    UnreadChatController unreadChatController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(unreadChatController).build();
    }

    @DisplayName("사용자는 자신에게 수신된 메세지 중에 확인하지 않은 메세지에 대한 정보를 조회할 수 있다.")
    @Test
    void userCanGetUnreadChatsInfo() {
        // given
        UserDetailsImpl userDetails = new UserDetailsImpl(
                UserResponse.builder()
                        .username("Test username")
                        .role(LAB)
                        .build());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        UnreadChatResponse unreadChatResponse = UnreadChatResponse.builder()
                .chatId("Test chat id")
                .unreadUsername("Test username what don't read")
                .sender("Test sender")
                .content("Test originalContent")
                .build();

        // when
        when(unreadChatService.getOwnByUserDetails(any(UserDetailsImpl.class)))
                .thenReturn(Flux.just(unreadChatResponse));

        // then
        webTestClient.mutate()
                .filter((request, next) -> next.exchange(request)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .build()
                .get()
                .uri("/api/unread-chat/info")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UnreadChatResponse.class)
                .hasSize(1);
    }
}