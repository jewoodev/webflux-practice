package com.heri2go.chat.domain.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class UnreadChat {
    @Id
    private String id;
    private String chatId;

    @Indexed(unique = true)
    private String unreadUsername;
    private String sender;

    @Builder
    private UnreadChat(String chatId, String unreadUsername, String sender) {
        this.chatId = chatId;
        this.unreadUsername = unreadUsername;
        this.sender = sender;
    }

    public static Flux<UnreadChat> from(Chat chat) {
        return Flux.fromIterable(chat.getUnreadUsernames())
                .map(unreadUsername -> UnreadChat.builder()
                        .chatId(chat.getId())
                        .unreadUsername(unreadUsername)
                        .sender(chat.getSender())
                        .build());
    }
}
