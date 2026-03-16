package com.heri2go.chat.domain.chat;

import com.heri2go.chat.JpaTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRepositoryTest extends JpaTestSupport {

    @AfterEach
    void tearDown() {
        chatRepository.deleteAllInBatch();
    }

    @DisplayName("채팅은 유효한 채팅방 id 값으로 조회될 수 있다.")
    @Test
    void chatCanGet_byValidRoomId() {
        // given
        Chat chat1 = Chat.builder()
                .roomId(1L)
                .content("첫 번째 메시지")
                .build();

        Chat chat2 = Chat.builder()
                .roomId(1L)
                .content("두 번째 메시지")
                .build();

        Chat chat3 = Chat.builder()
                .roomId(1L)
                .content("세 번째 메시지")
                .build();

        Chat chat4 = Chat.builder()
                .roomId(2L)
                .content("다른 방 메시지")
                .build();

        // 데이터 저장
        chatRepository.saveAll(List.of(chat1, chat2, chat3, chat4));

        // when
        List<Chat> chats = chatRepository.findByRoomId(1L);

        // then
        assertThat(chats).hasSize(3);
        assertThat(chats.get(0).getContent()).isEqualTo("첫 번째 메시지");
        assertThat(chats.get(1).getContent()).isEqualTo("두 번째 메시지");
        assertThat(chats.get(2).getContent()).isEqualTo("세 번째 메시지");
    }

    @DisplayName("채팅은 유효하지 채팅방 id 값으로 조회될 수 없다다.")
    @Test
    void chatCanNotGet_byInvalidRoomId() {
        // given
        Chat chat1 = Chat.builder()
                .roomId(1L)
                .content("첫 번째 메시지")
                .build();

        Chat chat2 = Chat.builder()
                .roomId(1L)
                .content("두 번째 메시지")
                .build();

        Chat chat3 = Chat.builder()
                .roomId(1L)
                .content("세 번째 메시지")
                .build();

        Chat chat4 = Chat.builder()
                .roomId(2L)
                .content("다른 방 메시지")
                .build();

        // 데이터 저장
        chatRepository.saveAll(List.of(chat1, chat2, chat3, chat4));

        // when
        List<Chat> chats = chatRepository.findByRoomId(3L);

        // then
        assertThat(chats).isEmpty();
    }
}
