package com.heri2go.chat.web.service.chat.response;

import com.heri2go.chat.domain.chat.Chat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatResponse(
        String sender,
        String msg,
        Long roomNum,
        Double sentimentScore,
        LocalDateTime createdAt
) {

    public static ChatResponse fromEntity(Chat chat) {
        return ChatResponse.builder()
                .sender(chat.getSender())
                .msg(chat.getMsg())
                .roomNum(chat.getRoomNum())
                .sentimentScore(chat.getSentimentScore())
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
