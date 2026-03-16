package com.heri2go.chat.domain.chat;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chats")
public class Chat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String sender;

    @ElementCollection
    @CollectionTable(name = "chat_unread_usernames", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "username")
    private Set<String> unreadUsernames;

    @Column(name = "room_id")
    private Long roomId;
    private String lang;
    private Double sentimentScore;

    @Builder
    private Chat(String content, String sender, Set<String> unreadUsernames,
                    Long roomId, String lang, Double sentimentScore) {
        this.content = content;
        this.sender = sender;
        this.unreadUsernames = unreadUsernames;
        this.roomId = roomId;
        this.lang = lang;
        this.sentimentScore = sentimentScore;
    }

    public static Chat from(ChatCreateRequest req) {
        return Chat.builder()
                .content(req.content())
                .sender(req.sender())
                .unreadUsernames(req.unreadUsernames())
                .roomId(req.roomId())
                .lang(req.lang())
                .sentimentScore(null)
                .build();
    }
}
