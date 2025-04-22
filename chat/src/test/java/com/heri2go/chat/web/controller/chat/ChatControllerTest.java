package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.TranslateService;
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

    @Mock
    private ChatService chatService;

    @Mock
    private TranslateService translateService;

    @InjectMocks
    private ChatController chatController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(chatController).build();
    }

    @DisplayName("방 번호에 알맞은 메세지들을 제공한다.")
    @Test
    void getChatHistory_shouldReturnChatMessages() {
        // given
        Long roomNum = 1L;
        ChatResponse message1 = ChatResponse.builder()
                .roomNum(roomNum)
                .sender("user1")
                .msg("안녕하세요")
                .createdAt(LocalDateTime.now())
                .build();

        ChatResponse message2 = ChatResponse.builder()
                .roomNum(roomNum)
                .sender("user2")
                .msg("Hi there")
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

//     @DisplayName("전송한 메세지는 번역되어 저장된다.")
//     @Test
//     void sendMessage_shouldTranslateAndSaveMessage() {
//         // given
//         Long chatRoomId = 1L;
//         ChatMessageReq req = ChatMessageReq.builder()
//                 .sender("user1")
//                 .msg("안녕하세요")
//                 .lang("ko")
//                 .roomNum(chatRoomId)
//                 .build();

//         ChatMessageResp expectedResponse = ChatMessageResp.builder()
//                 .roomNum(chatRoomId)
//                 .sender("user1")
//                 .msg("Hello")
//                 .createdAt(LocalDateTime.now())
//                 .build();

//         // when
//         when(translateService.translate(eq("안녕하세요"), eq("ko"), eq("en")))
//                 .thenReturn(Mono.just("Hello"));
//         when(chatService.save(any(ChatMessageReq.class)))
//                 .thenReturn(Mono.just(expectedResponse));

//         // then
//         Mono<ChatMessageResp> result = chatController.sendMessage(req, chatRoomId);

//         StepVerifier.create(result)
//                 .expectNextMatches(response ->
//                         response.sender().equals("user1") &&
//                                 response.msg().equals("Hello")
//                 )
//                 .verifyComplete();
//     }
}