package com.heri2go.chat.domain.chat;

import com.heri2go.chat.domain.chat.dto.ChatMessageReq;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "chat")
public class Chat {

    @Id
    private String id;
    private String msg;
    private String sender;
    private Long roomNum;
    private String lang; // 채팅을 작성한 유저가 사용한 언어

    private LocalDateTime createdAt;

    @Builder
    private Chat(String msg, String sender, Long roomNum, String lang, LocalDateTime createdAt) {
        this.msg = msg;
        this.sender = sender;
        this.roomNum = roomNum;
        this.lang = lang;
        this.createdAt = createdAt;
    }

    public static Chat fromReq(ChatMessageReq req) { // 메세지 Request 로부터 최초로 생성하는 Chat
        return Chat.builder()
                .msg(req.getMsg())
                .sender(req.getSender())
                .roomNum(req.getRoomNum())
                .lang(req.getLang())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
