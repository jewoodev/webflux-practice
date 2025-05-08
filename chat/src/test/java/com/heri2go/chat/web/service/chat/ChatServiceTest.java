package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.MockTestSupport;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.exception.ResourceNotFoundException;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.user.response.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ChatServiceTest extends MockTestSupport {

    @Mock
    private ChatRepository chatRepository;

    @InjectMocks
    private ChatService chatService;

    private Chat sampleChat;
    private ChatCreateRequest sampleReq;
    private final String testUsername = "testUser";
    private final String testRoomId = "101";

    @BeforeEach
    void setUp() {
        // 테스트용 샘플 데이터 생성
        sampleChat = Chat.builder()
                .sender(testUsername)
                .content("Hello, WebFlux!")
                .roomId(testRoomId)
                .unreadUsernames(Set.of(testUsername))
                .lang("en")
                .createdAt(LocalDateTime.now())
                .build();

        sampleReq = ChatCreateRequest.builder()
                .sender(testUsername)
                .content("Hello, WebFlux!")
                .unreadUsernames(Set.of(testUsername))
                .roomId(testRoomId)
                .lang("en")
                .build();
    }

    @DisplayName("정상적인 메세지를 저장하면 정상적으로 응답 Dto를 돌려받는다.")
    @Test
    void save_validMessage_shouldReturnChatMessageResp() {
        // Given // When
        when(chatRepository.save(any(Chat.class))).thenReturn(Mono.just(sampleChat));

        // Then
        StepVerifier.create(chatService.save(sampleReq))
                .expectNextMatches(chat -> chat.getRoomId().equals(sampleReq.roomId()) &&
                        chat.getSender().equals(sampleReq.sender()) &&
                        chat.getContent().equals(sampleReq.content()))
                .verifyComplete();
    }

    @DisplayName("유효한 채팅방 번호로 조회되는 채팅들이 정상적으로 조회된다.")
    @Test
    void getByRoomNum_validRoomNum_shouldReturnChatMessageRespFlux() {
        // Given
        UserDetailsImpl testUser = new UserDetailsImpl(UserResponse.builder().username("testUser").build());
        when(chatRepository.findByRoomId("101"))
                .thenReturn(Flux.just(sampleChat));

        // When
        Flux<ChatResponse> result = chatService.getByRoomIdToInvited("101", testUser);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(resp -> resp.roomId().equals(testRoomId) &&
                        resp.sender().equals(testUsername) &&
                        resp.content().equals("Hello, WebFlux!"))
                .verifyComplete();
    }

    @DisplayName("채팅이 없는 채팅방을 조회하면 0개의 채팅이 결과로 나온다.")
    @Test
    void getEmptyChat_IfNonChatRoomIsQueried() {
        // Given
        UserDetailsImpl testUser = new UserDetailsImpl(UserResponse.builder().username("testUser").build());

        // When
        when(chatRepository.findByRoomId("999"))
                .thenReturn(Flux.empty());

        // Then
        StepVerifier.create(chatService.getByRoomIdToInvited("999", testUser))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
