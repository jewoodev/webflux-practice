package com.heri2go.chat.domain.chat;

import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Chat {

    @Id
    private String id;
    private String content;
    private String sender;
    private Set<String> unreadUsernames;

    @Indexed
    private String roomId;
    private String lang; // 채팅을 작성한 유저가 사용한 언어
    private Double sentimentScore;

    private LocalDateTime createdAt;

    @Builder
    private Chat(String content, String sender, Set<String> unreadUsernames, 
                    String roomId, String lang, Double sentimentScore, 
                    LocalDateTime createdAt) {
        this.content = content;
        this.sender = sender;
        this.unreadUsernames = unreadUsernames;
        this.roomId = roomId;
        this.lang = lang;
        this.sentimentScore = sentimentScore;
        this.createdAt = createdAt;
    }

    public static Chat from(ChatCreateRequest req) { // 메세지 Request 로부터 최초로 생성하는 Chat
        return Chat.builder()
                .content(req.content())
                .sender(req.sender())
                .unreadUsernames(req.unreadUsernames())
                .roomId(req.roomId())
                .lang(req.lang())
                .sentimentScore(null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
