package com.heri2go.chat.domain.chat;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "chat")
public class Chat {

    @Id
    private String id;
    private String content;
    private String sender;
    private Long roomNum;
    private String lang; // 채팅을 작성한 유저가 사용한 언어
    private Double sentimentScore;

    private LocalDateTime createdAt;

    @Builder
    private Chat(String content, String sender, Long roomNum, String lang, Double sentimentScore, LocalDateTime createdAt) {
        this.content = content;
        this.sender = sender;
        this.roomNum = roomNum;
        this.lang = lang;
        this.sentimentScore = sentimentScore;
        this.createdAt = createdAt;
    }

    public static Chat fromReq(ChatCreateRequest req) { // 메세지 Request 로부터 최초로 생성하는 Chat
        return Chat.builder()
                .content(req.content())
                .sender(req.sender())
                .roomNum(req.roomNum())
                .lang(req.lang())
                .sentimentScore(null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
