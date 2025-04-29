package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class ChatControllerTest extends MockTestSupport {

    private WebTestClient webTestClient;

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatRestController chatRestController;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(chatRestController).build();
    }

    @DisplayName("방 번호에 알맞은 메세지들을 제공한다.")
    @Test
    void getChatHistory_shouldReturnChatMessages() {
        // given
        Long roomNum = 1L;
        ChatResponse message1 = ChatResponse.builder()
                .roomNum(roomNum)
                .sender("user1")
                .content("안녕하세요")
                .createdAt(LocalDateTime.now())
                .build();

        ChatResponse message2 = ChatResponse.builder()
                .roomNum(roomNum)
                .sender("user2")
                .content("Hi there")
                .createdAt(LocalDateTime.now())
                .build();

        // when
        when(chatService.getByRoomNum(eq(roomNum))).thenReturn(Flux.just(message1, message2));

        // then
        webTestClient.get()
                .uri("/chat/{roomNum}", roomNum)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ChatResponse.class)
                .hasSize(2)
                .contains(message1, message2);
    }
}