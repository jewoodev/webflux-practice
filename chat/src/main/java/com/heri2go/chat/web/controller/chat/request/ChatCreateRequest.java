package com.heri2go.chat.web.controller.chat.request;

import java.util.Set;

import lombok.Builder;

@Builder
public record ChatCreateRequest(
    String content,
    String sender,
    Set<String> unreadUsernames,
    String roomId,
    String lang,
    MessageType type
) {
    public static ChatCreateRequest withTranslatedMsg(ChatCreateRequest req, String translatedMessage) {
        return ChatCreateRequest.builder()
                .content(translatedMessage)
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
