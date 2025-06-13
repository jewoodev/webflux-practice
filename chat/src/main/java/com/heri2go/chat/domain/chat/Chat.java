package com.heri2go.chat.domain.chat;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Chat extends BaseTimeEntity {

    @Id
    private String id;
    private String originalContent;
    private String translatedContent;
    private String sender;
    private Set<String> unreadUsernames; // 읽음 처리할 용도

    @Indexed
    private String roomId;
    private String lang; // 채팅을 작성한 유저가 사용한 언어
    private Double sentimentScore;

    @Builder
    private Chat(String originalContent, String translatedContent, String sender, Set<String> unreadUsernames,
                 String roomId, String lang, Double sentimentScore) {
        this.originalContent = originalContent;
        this.translatedContent = translatedContent;
        this.sender = sender;
        this.unreadUsernames = unreadUsernames;
        this.roomId = roomId;
        this.lang = lang;
        this.sentimentScore = sentimentScore;
    }

    public static Chat from(ChatCreateRequest req) { // 메세지 Request 로부터 최초로 생성하는 Chat
        return Chat.builder()
                .originalContent(req.originalContent())
                .translatedContent(req.translatedContent())
                .sender(req.sender())
                .unreadUsernames(req.unreadUsernames())
                .roomId(req.roomId())
                .lang(req.lang())
                .sentimentScore(null)
                .build();
    }
}
