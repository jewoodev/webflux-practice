package com.heri2go.chat.domain.chat;

import com.heri2go.chat.JpaTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UnreadChatRepositoryTest extends JpaTestSupport {

    @AfterEach
    void tearDown() {
        unreadChatRepository.deleteAllInBatch();
    }

    @DisplayName("'읽지 않은 채팅 정보'는 '유효한 읽지 않은 username' 값으로 조회할 수 있다.")
    @Test
    void unreadChatsCanReferred_byValidUnreadUsername() {
        // given
        String testUnreadUsername = "Test username who don't read chat";
        Long testChatId = 1L;
        String testSender = "Test sender";
        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId(testChatId)
                .unreadUsername(testUnreadUsername)
                .sender(testSender)
                .build();

        unreadChatRepository.save(testUnreadChat);

        // when
        List<UnreadChat> unreadChats = unreadChatRepository.findAllByUnreadUsername(testUnreadUsername);

        // then
        assertThat(unreadChats).hasSize(1);
        assertThat(unreadChats.get(0).getUnreadUsername()).isEqualTo(testUnreadUsername);
        assertThat(unreadChats.get(0).getChatId()).isEqualTo(testChatId);
        assertThat(unreadChats.get(0).getSender()).isEqualTo(testSender);
    }

    @DisplayName("'읽지 않은 채팅 정보'는 '유효하지 않은 읽지 않은 username' 값으로 조회할 수 없다.")
    @Test
    void unreadChatsCanNotBeReferred_byInvalidUnreadUsername() {
        // given
        String testUnreadUsername = "Test username who don't read chat";
        Long testChatId = 1L;
        String testSender = "Test sender";
        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId(testChatId)
                .unreadUsername(testUnreadUsername)
                .sender(testSender)
                .build();

        unreadChatRepository.save(testUnreadChat);

        // when
        List<UnreadChat> unreadChats = unreadChatRepository.findAllByUnreadUsername("Wrong username who don't read chat");

        // then
        assertThat(unreadChats).isEmpty();
    }
}
