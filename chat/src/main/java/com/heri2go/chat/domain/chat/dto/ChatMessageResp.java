package com.heri2go.chat.domain.chat.dto;

import com.heri2go.chat.domain.chat.Chat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatMessageResp(
        String sender,
        String msg,
        Long roomNum,
        LocalDateTime createdAt
) {

    public static ChatMessageResp fromEntity(Chat chat) {
        return ChatMessageResp.builder()
                .sender(chat.getSender())
                .msg(chat.getMsg())
                .roomNum(chat.getRoomNum())
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
