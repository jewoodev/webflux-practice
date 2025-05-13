package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

class ChatRoomParticipantRepositoryTest extends MongoTestSupport {

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(ChatRoomParticipant.class)
                .then(mongoTemplate.createCollection(ChatRoomParticipant.class))
                .block();
    }

    @DisplayName("채팅방 참여자는 저장되고 계정의 식별자(user id)를 통해 조회될 수 있다.")
    @Test
    void chatRoomParticipantCanBeSaved_and_canBeReferred_byUserId() {
        // given
        String testUserId = "Test user id";
        String testUsername = "Test username";
        String testChatRoomId = "Test chat room id";
        chatRoomParticipantRepository.save(
                ChatRoomParticipant.builder()
                        .userId(testUserId)
                        .username(testUsername)
                        .chatRoomId(testChatRoomId)
                        .joinedAt(LocalDateTime.now())
                        .build()
                )
                .block();

        // when
        StepVerifier.create(chatRoomParticipantRepository.findAllByUserId(testUserId))
                // then
                .expectNextMatches(chatRoomParticipant -> chatRoomParticipant.getUserId().equals(testUserId) &&
                        chatRoomParticipant.getUsername().equals(testUsername) &&
                                chatRoomParticipant.getChatRoomId().equals(testChatRoomId)
                )
                .verifyComplete();
    }
}