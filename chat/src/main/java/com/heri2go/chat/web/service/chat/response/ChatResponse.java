package com.heri2go.chat.web.service.chat.response;

import com.heri2go.chat.domain.chat.Chat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ChatResponse(
        String content,
        String sender,
        Set<String> unreadUsername,
        String roomId,
        Double sentimentScore,
        LocalDateTime createdAt
) {

    public static ChatResponse from(Chat chat) {
        return ChatResponse.builder()
                .sender(chat.getSender())
                .content(chat.getContent())
                .unreadUsername(chat.getUnreadUsernames())
                .roomId(chat.getRoomId())
                .sentimentScore(chat.getSentimentScore())
                .createdAt(chat.getCreatedAt())
                .build();
    }
}
