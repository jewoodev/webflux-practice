package com.heri2go.chat.domain.chat;

import com.heri2go.chat.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "unread_chats")
public class UnreadChat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;

    @Column(name = "unread_username")
    private String unreadUsername;
    private String sender;
    private String content;

    @Builder
    private UnreadChat(Long chatId, String unreadUsername, String sender, String content) {
        this.chatId = chatId;
        this.unreadUsername = unreadUsername;
        this.sender = sender;
        this.content = content;
    }

    public static List<UnreadChat> from(Chat chat) {
        return chat.getUnreadUsernames().stream()
                .map(unreadUsername -> UnreadChat.builder()
                        .chatId(chat.getId())
                        .unreadUsername(unreadUsername)
                        .sender(chat.getSender())
                        .content(chat.getContent())
                        .build())
                .toList();
    }
}
