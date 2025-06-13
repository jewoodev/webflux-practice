package com.heri2go.chat.web.controller.chat.request;

import lombok.Builder;

import java.util.Set;

@Builder
public record ChatCreateRequest(
    String originalContent,
    String translatedContent,
    String sender,
    Set<String> unreadUsernames,
    String roomId,
    String lang,
    MessageType type
) {
    public static ChatCreateRequest withTranslatedMsg(ChatCreateRequest req, String translatedMessage) {
        return ChatCreateRequest.builder()
                .originalContent(req.originalContent())
                .translatedContent(translatedMessage)
                .sender(req.sender())
                .unreadUsernames(req.unreadUsernames())
                .roomId(req.roomId())
                .lang(req.lang())
                .type(req.type())
                .build();
    }

    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}
