package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.JpaTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomParticipantRepositoryTest extends JpaTestSupport {

    @AfterEach
    void tearDown() {
        chatRoomParticipantRepository.deleteAllInBatch();
    }

    @DisplayName("채팅방 참여자는 저장되고 계정의 식별자(user id)를 통해 조회될 수 있다.")
    @Test
    void chatRoomParticipantCanBeSaved_and_canBeReferred_byUserId() {
        // given
        Long testUserId = 1L;
        String testUsername = "Test username";
        Long testChatRoomId = 1L;
        chatRoomParticipantRepository.save(
                ChatRoomParticipant.builder()
                        .userId(testUserId)
                        .username(testUsername)
                        .chatRoomId(testChatRoomId)
                        .build()
        );

        // when
        List<ChatRoomParticipant> participants = chatRoomParticipantRepository.findAllByUserId(testUserId);

        // then
        assertThat(participants).hasSize(1);
        assertThat(participants.get(0).getUserId()).isEqualTo(testUserId);
        assertThat(participants.get(0).getUsername()).isEqualTo(testUsername);
        assertThat(participants.get(0).getChatRoomId()).isEqualTo(testChatRoomId);
    }
}
