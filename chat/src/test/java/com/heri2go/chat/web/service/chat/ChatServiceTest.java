package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatServiceTest extends MockTestSupport {

        @Mock
        private ChatRepository chatRepository;

        @InjectMocks
        private ChatService chatService;

        private Chat sampleChat;
        private ChatCreateRequest sampleReq;

        @BeforeEach
        void setUp() {
                // 테스트용 샘플 데이터 생성
                sampleChat = Chat.builder()
                                .sender("testUser")
                                .content("Hello, WebFlux!")
                                .roomNum(101L)
                                .lang("en")
                                .createdAt(LocalDateTime.now())
                                .build();

                sampleReq = ChatCreateRequest.builder()
                                .sender("testUser")
                                .content("Hello, WebFlux!")
                                .roomNum(101L)
                                .lang("en")
                                .build();
        }

        @DisplayName("메세지를 저장하면 정상적으로 응답 Dto를 돌려받는다.")
        @Test
        void save_shouldReturnChatMessageResp() {
                // Given // When
                when(chatRepository.save(any(Chat.class))).thenReturn(Mono.just(sampleChat));

                // Then
                StepVerifier.create(chatService.save(sampleReq))
                                .expectNextMatches(resp -> resp.roomNum().equals(sampleReq.roomNum()) &&
                                                resp.sender().equals(sampleReq.sender()) &&
                                                resp.content().equals(sampleReq.content()))
                                .verifyComplete();
        }

        @DisplayName("채팅방 번호로 조회되는 채팅들이 정상적으로 조회된다.")
        @Test
        void getByRoomNum_shouldReturnChatMessageRespFlux() {
                // Given
                when(chatRepository.findByRoomNumOrderByCreatedAt(101L))
                                .thenReturn(Flux.just(sampleChat));

                // When
                Flux<ChatResponse> result = chatService.getByRoomNum(101L);

                // Then
                StepVerifier.create(result)
                                .expectNextMatches(resp -> resp.roomNum().equals(101L) &&
                                                resp.sender().equals("testUser") &&
                                                resp.content().equals("Hello, WebFlux!"))
                                .verifyComplete();
        }

        @DisplayName("채팅이 없는 채팅방 번호로 조회를 하면 0개의 채팅이 결과로 나온다.")
        @Test
        void getByRoomNum_shouldReturnEmptyFluxForNonExistingRoom() {
                // Given
                when(chatRepository.findByRoomNumOrderByCreatedAt(999L))
                                .thenReturn(Flux.empty());

                // When
                Flux<ChatResponse> result = chatService.getByRoomNum(999L);

                // Then
                StepVerifier.create(result)
                                .expectNextCount(0)
                                .verifyComplete();
        }
}
