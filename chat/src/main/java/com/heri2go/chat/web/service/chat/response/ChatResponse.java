package com.heri2go.chat.web.service.chat.response;

import com.heri2go.chat.domain.chat.Chat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
public class ChatResponse {

    private String originalContent;
    private String translatedContent;
    private String sender;
    private Set<String> unreadUsername;
    private String roomId;
    private Double sentimentScore;
    private LocalDateTime createdAt;

    private ChatResponse(String originalContent, String translatedContent, String sender,
                         Set<String> unreadUsername, String roomId, Double sentimentScore, LocalDateTime createdAt) {
        this.originalContent = originalContent;
        this.translatedContent = translatedContent;
        this.sender = sender;
        this.unreadUsername = unreadUsername;
        this.roomId = roomId;
        this.sentimentScore = sentimentScore;
        this.createdAt = createdAt;
    }

    public static ChatResponse from(Chat chat) {
        return new ChatResponse(
                chat.getOriginalContent(),
                chat.getTranslatedContent(),
                chat.getSender(),
                chat.getUnreadUsernames(),
                chat.getRoomId(),
                chat.getSentimentScore(),
                chat.getCreatedAt()
        );
    }
}
