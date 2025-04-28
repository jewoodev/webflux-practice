package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

class ChatRoomRepositoryTest extends MongoTestSupport {

    private LocalDateTime now = LocalDateTime.now();

    @AfterEach
    void tearDown() {
        reactiveMongoTemplate.dropCollection(ChatRoom.class)
                .then(reactiveMongoTemplate.createCollection(ChatRoom.class))
                .block();
        reactiveMongoTemplate.dropCollection(ChatRoomParticipant.class)
                .then(reactiveMongoTemplate.createCollection(ChatRoomParticipant.class))
                .block();
    }

    @BeforeEach
    void setUp() {
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName("test")
                .participantIds(Set.of("user1", "user2"))
                .lastMessage("testMessage")
                .lastSender("user1")
                .lastMessageTime(now.minusHours(1))
                .build();

        chatRoom = chatRoomRepository.save(chatRoom).block();

        ChatRoomParticipant chatRoomParticipant = ChatRoomParticipant.builder()
                .username("user1")
                .chatRoomId(chatRoom.getId())
                .joinedAt(now.minusHours(1))
                .build();

        ChatRoomParticipant chatRoomParticipant2 = ChatRoomParticipant.builder()
                .username("user2")
                .chatRoomId(chatRoom.getId())
                .joinedAt(now.minusHours(3))
                .build();

        chatRoomParticipantRepository.saveAll(Flux.just(chatRoomParticipant, chatRoomParticipant2)).blockLast();
    }

    @DisplayName("유저는 자신이 참여 중인 채팅방 목록과 채팅방 정보를 조회할 수 있다.")
    @Test
    void findByParticipantsContaining() {
        // given // when -> setUp()
        String userId = "user1";
        Flux<ChatRoom> chatRoomFlux = chatRoomParticipantRepository.findByUsername(userId)
                                                .flatMap(chatRoomParticipant -> chatRoomRepository.findById(chatRoomParticipant.getChatRoomId()
                                        ));

        // then
        StepVerifier.create(chatRoomFlux)
                    .expectNextMatches(chatRoom -> 
                        chatRoom.getRoomName().equals("test") &&
                        chatRoom.getParticipantIds().contains(userId) &&
                        chatRoom.getLastMessage().equals("testMessage") &&
                        chatRoom.getLastSender().equals("user1") &&
                        chatRoom.getLastMessageTime().truncatedTo(ChronoUnit.SECONDS)
                                .isEqual(now.minusHours(1).truncatedTo(ChronoUnit.SECONDS))
                    )
                    .verifyComplete();
    }
}