package com.heri2go.chat.domain.chat;

import com.heri2go.chat.MongoTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UnreadChatRepositoryTest extends MongoTestSupport {

    @AfterEach
    void tearDown() {
        reactiveMongoTemplate.dropCollection(UnreadChat.class)
                .then(reactiveMongoTemplate.createCollection(UnreadChat.class))
                .block();
    }

    @DisplayName("읽지 않은 메세지 정보는 읽지 않은 username 값으로 조회할 수 있다.")
    @Test
    void unreadChatsCanRefered_throughUnreadUsername() {
        // given // when
        String testUnreadUsername = "Test Unread Username";
        UnreadChat testUnreadChat = UnreadChat.builder()
                .chatId("Test Chat Id")
                .unreadUsername(testUnreadUsername)
                .sender("Test Sender")
                .build();

        unreadChatRepository.save(testUnreadChat)
                .block();

        // then
        StepVerifier.create(unreadChatRepository.findByUnreadUsername(testUnreadUsername))
                .expectNextMatches(unreadChat -> unreadChat.getUnreadUsername().equals(testUnreadUsername)
                        && unreadChat.getChatId().equals("Test Chat Id")
                        && unreadChat.getSender().equals("Test Sender"))
                .verifyComplete();
    }
}