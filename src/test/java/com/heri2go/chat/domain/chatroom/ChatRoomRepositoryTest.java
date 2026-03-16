package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Set;

class ChatRoomRepositoryTest extends MongoTestSupport {

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(ChatRoom.class)
                .then(mongoTemplate.createCollection(ChatRoom.class))
                .block();
    }

    @DisplayName("채팅방은 저장되고 id 값으로 조회될 수 있다.")
    @Test
    void chatRoomCanBeSaved_and_canBeReferred_by_id() {
        // given // when
        String testParticipantId1 = "Test participant id 1";
        String testParticipantId2 = "Test participant id 2";
        ChatRoom testChatRoom = ChatRoom.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(Set.of(testParticipantId1, testParticipantId2))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .build();

        // then
        StepVerifier.create(chatRoomRepository.save(testChatRoom)
                        .flatMap(chatRoom -> chatRoomRepository.findById(chatRoom.getId()))
                )
                .expectNextMatches(chatRoom ->
                        chatRoom.getOrderId().equals(testChatRoom.getOrderId()) &&
                                chatRoom.getRoomName().equals(testChatRoom.getRoomName()) &&
                                chatRoom.getParticipantIds().contains(testParticipantId1) &&
                                chatRoom.getParticipantIds().contains(testParticipantId2) &&
                                chatRoom.getLastMessage().contains(testChatRoom.getLastMessage()) &&
                                chatRoom.getLastSender().contains(testChatRoom.getLastSender())
                )
                .verifyComplete();
    }

    @DisplayName("유효하지 않은 id로 채팅방을 조회하면 아무런 데이터도 조회되지 않는다.")
    @Test
    void chatRoomCanNotBeReferred_byInvalidId() {
        // given // when // then
        String testParticipantId1 = "Test participant id 1";
        String testParticipantId2 = "Test participant id 2";
        ChatRoom testChatRoom = ChatRoom.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(Set.of(testParticipantId1, testParticipantId2))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .build();

        StepVerifier.create(chatRoomRepository.save(testChatRoom)
                .flatMap(chatRoom -> chatRoomRepository.findById("Wrong id"))
        ).expectComplete();
    }
}