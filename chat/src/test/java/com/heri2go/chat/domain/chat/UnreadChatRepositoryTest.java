package com.heri2go.chat.domain.chat;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UnreadChatRepositoryTest extends MongoTestSupport {

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(UnreadChat.class)
                .then(mongoTemplate.createCollection(UnreadChat.class))
                .block();
    }

    @DisplayName("'읽지 않은 채팅 정보'는 '유효한 읽지 않은 username' 값으로 조회할 수 있다.")
    @Test
    void unreadChatsCanReferred_byValidUnreadUsername() {
        // given // when
        String testUnreadUsername = "Test username who don't read chat";
        String testChatId = "Test chat id";
        String testSender = "Test sender";
        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId(testChatId)
                .unreadUsername(testUnreadUsername)
                .sender(testSender)
                .build();

        unreadChatRepository.save(testUnreadChat)
                .block();

        // then
        StepVerifier.create(unreadChatRepository.findAllByUnreadUsername(testUnreadUsername))
                .expectNextMatches(unreadChat ->
                        unreadChat.getUnreadUsername().equals(testUnreadUsername)
                        && unreadChat.getChatId().equals(testChatId)
                        && unreadChat.getSender().equals(testSender)
                )
                .verifyComplete();
    }

    @DisplayName("'읽지 않은 채팅 정보'는 '유효하지 않은 읽지 않은 username' 값으로 조회할 수 없다.")
    @Test
    void test() {
        // given // when
        String testUnreadUsername = "Test username who don't read chat";
        String testChatId = "Test chat id";
        String testSender = "Test sender";
        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId(testChatId)
                .unreadUsername(testUnreadUsername)
                .sender(testSender)
                .build();

        unreadChatRepository.save(testUnreadChat)
                .block();

        // then
        StepVerifier.create(unreadChatRepository.findAllByUnreadUsername("Wrong username who don't read chat"))
                .expectComplete();
    }
}