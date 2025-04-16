package com.heri2go.chat.domain;

import com.heri2go.chat.domain.dto.ChatMessageReq;
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
    private String lang;

    private LocalDateTime createdAt;

    @Builder
    private Chat(String msg, String sender, Long roomNum, String lang, LocalDateTime createdAt) {
        this.msg = msg;
        this.sender = sender;
        this.roomNum = roomNum;
        this.lang = lang;
        this.createdAt = createdAt;
    }

    public static Chat fromReq(ChatMessageReq req) {
        return Chat.builder()
                .msg(req.getMsg())
                .sender(req.getSender())
                .roomNum(req.getRoomNum())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
