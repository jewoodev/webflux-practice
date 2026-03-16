package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.JpaTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomRepositoryTest extends JpaTestSupport {

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAllInBatch();
    }

    @DisplayName("채팅방은 저장되고 id 값으로 조회될 수 있다.")
    @Test
    void chatRoomCanBeSaved_and_canBeReferred_by_id() {
        // given
        Long testParticipantId1 = 1L;
        Long testParticipantId2 = 2L;
        ChatRoom testChatRoom = ChatRoom.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(Set.of(testParticipantId1, testParticipantId2))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .build();

        // when
        ChatRoom savedChatRoom = chatRoomRepository.save(testChatRoom);
        Optional<ChatRoom> foundChatRoom = chatRoomRepository.findById(savedChatRoom.getId());

        // then
        assertThat(foundChatRoom).isPresent();
        ChatRoom chatRoom = foundChatRoom.get();
        assertThat(chatRoom.getOrderId()).isEqualTo("Test order id");
        assertThat(chatRoom.getRoomName()).isEqualTo("Test room name");
        assertThat(chatRoom.getParticipantIds()).contains(testParticipantId1, testParticipantId2);
        assertThat(chatRoom.getLastMessage()).isEqualTo("Test last message of chat room");
        assertThat(chatRoom.getLastSender()).isEqualTo("Test last sender of chat room");
    }

    @DisplayName("유효하지 않은 id로 채팅방을 조회하면 아무런 데이터도 조회되지 않는다.")
    @Test
    void chatRoomCanNotBeReferred_byInvalidId() {
        // given
        Long testParticipantId1 = 1L;
        Long testParticipantId2 = 2L;
        ChatRoom testChatRoom = ChatRoom.builder()
                .orderId("Test order id")
                .roomName("Test room name")
                .participantIds(Set.of(testParticipantId1, testParticipantId2))
                .lastMessage("Test last message of chat room")
                .lastSender("Test last sender of chat room")
                .build();

        chatRoomRepository.save(testChatRoom);

        // when
        Optional<ChatRoom> foundChatRoom = chatRoomRepository.findById(999L);

        // then
        assertThat(foundChatRoom).isEmpty();
    }
}
