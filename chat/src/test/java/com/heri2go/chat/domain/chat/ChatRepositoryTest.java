package com.heri2go.chat.domain.chat;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

class ChatRepositoryTest extends MongoTestSupport {

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now().withNano(0);

        Chat chat1 = Chat.builder()
                .roomId("1")
                .content("첫 번째 메시지")
                .createdAt(now.minusMinutes(2))
                .build();

        Chat chat2 = Chat.builder()
                .roomId("1")
                .content("두 번째 메시지")
                .createdAt(now.minusMinutes(1))
                .build();

        Chat chat3 = Chat.builder()
                .roomId("1")
                .content("세 번째 메시지")
                .createdAt(now)
                .build();

        Chat chat4 = Chat.builder()
                .roomId("2")
                .content("다른 방 메시지")
                .createdAt(now)
                .build();


        // 데이터 저장
        chatRepository.saveAll(Arrays.asList(chat1, chat2, chat3, chat4))
                .blockLast();
    }

    @AfterEach
    public void tearDown() {
        mongoTemplate.dropCollection(Chat.class)
                .then(mongoTemplate.createCollection(Chat.class))
                .block();
    }

    @DisplayName("채팅은 유효한 채팅방 id 값으로 조회될 수 있다.")
    @Test
    public void chatCanGet_byValidRoomId() {
        // then
        StepVerifier.create(chatRepository.findByRoomId("1"))
                .expectNextMatches(chat -> chat.getContent().equals("첫 번째 메시지"))
                .expectNextMatches(chat -> chat.getContent().equals("두 번째 메시지"))
                .expectNextMatches(chat -> chat.getContent().equals("세 번째 메시지"))
                .verifyComplete();
    }

    @DisplayName("채팅은 유효하지 채팅방 id 값으로 조회될 수 없다다.")
    @Test
    public void chatCanNotGet_byInvalidRoomId() {
        // then
        StepVerifier.create(chatRepository.findByRoomId("3"))
                .expectComplete();
    }
}