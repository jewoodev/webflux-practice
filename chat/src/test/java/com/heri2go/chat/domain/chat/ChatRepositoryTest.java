package com.heri2go.chat.domain.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

@DataMongoTest
class ChatRepositoryTest {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @BeforeEach
    public void setup() {
        // 테스트 전에 컬렉션을 비웁니다
        reactiveMongoTemplate.dropCollection(Chat.class)
                .then(reactiveMongoTemplate.createCollection(Chat.class))
                .block();
    }


    @Test
    public void testFindByRoomNumOrderByCreatedAt() {
        // 테스트 데이터 준비
        LocalDateTime now = LocalDateTime.now();

        Chat chat1 = Chat.builder()
                .roomNum(1L)
                .msg("첫 번째 메시지")
                .createdAt(now.minusMinutes(2))
                .build();

        Chat chat2 = Chat.builder()
                .roomNum(1L)
                .msg("두 번째 메시지")
                .createdAt(now.minusMinutes(1))
                .build();

        Chat chat3 = Chat.builder()
                .roomNum(1L)
                .msg("세 번째 메시지")
                .createdAt(now)
                .build();

        Chat chat4 = Chat.builder()
                .roomNum(2L)
                .msg("다른 방 메시지")
                .createdAt(now)
                .build();


        // 데이터 저장
        chatRepository.saveAll(Arrays.asList(chat1, chat2, chat3, chat4))
                .blockLast();

        // 테스트 실행
        Flux<Chat> result = chatRepository.findByRoomNumOrderByCreatedAt(1L);

        // 검증
        StepVerifier.create(result)
                .expectNextMatches(chat -> chat.getMsg().equals("첫 번째 메시지"))
                .expectNextMatches(chat -> chat.getMsg().equals("두 번째 메시지"))
                .expectNextMatches(chat -> chat.getMsg().equals("세 번째 메시지"))
                .verifyComplete();
    }
}