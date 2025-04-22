package com.heri2go.chat.domain.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.heri2go.chat.MongoTestSupport;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;

class ChatRepositoryTest extends MongoTestSupport {
    
    @BeforeEach
    public void setup() {
        // 테스트 전에 컬렉션을 비웁니다
        reactiveMongoTemplate.dropCollection(Chat.class)
                .then(reactiveMongoTemplate.createCollection(Chat.class))
                .block();
    }

    @DisplayName("방 번호에 알맞은 메시지들을 제공한다.")
    @Test
    public void testFindByRoomNumOrderByCreatedAt() {
        // 테스트 데이터 준비
        LocalDateTime now = LocalDateTime.now();

        Chat chat1 = Chat.builder()
                .roomNum(1L)
                .content("첫 번째 메시지")
                .createdAt(now.minusMinutes(2))
                .build();

        Chat chat2 = Chat.builder()
                .roomNum(1L)
                .content("두 번째 메시지")
                .createdAt(now.minusMinutes(1))
                .build();

        Chat chat3 = Chat.builder()
                .roomNum(1L)
                .content("세 번째 메시지")
                .createdAt(now)
                .build();

        Chat chat4 = Chat.builder()
                .roomNum(2L)
                .content("다른 방 메시지")
                .createdAt(now)
                .build();


        // 데이터 저장
        chatRepository.saveAll(Arrays.asList(chat1, chat2, chat3, chat4))
                .blockLast();

        // 테스트 실행
        Flux<Chat> result = chatRepository.findByRoomNumOrderByCreatedAt(1L);

        // 검증
        StepVerifier.create(result)
                .expectNextMatches(chat -> chat.getContent().equals("첫 번째 메시지"))
                .expectNextMatches(chat -> chat.getContent().equals("두 번째 메시지"))
                .expectNextMatches(chat -> chat.getContent().equals("세 번째 메시지"))
                .verifyComplete();
    }
}