package com.heri2go.chat.domain.chat;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private UnreadChat(String chatId, String unreadUsername, String sender,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.chatId = chatId;
        this.unreadUsername = unreadUsername;
        this.sender = sender;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Flux<UnreadChat> from(Chat chat) {
        return Flux.fromIterable(chat.getUnreadUsernames())
                .map(unreadUsername -> UnreadChat.builder()
                        .chatId(chat.getId())
                        .unreadUsername(unreadUsername)
                        .sender(chat.getSender())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build());
    }
}
