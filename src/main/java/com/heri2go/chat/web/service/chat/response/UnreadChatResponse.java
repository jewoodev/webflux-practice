package com.heri2go.chat.web.service.chat.response;

import com.heri2go.chat.domain.chat.UnreadChat;
import lombok.Builder;

@Builder
public record UnreadChatResponse(
        String chatId,
        String unreadUsername,
        String sender,
        String content
) {
    public static UnreadChatResponse from(UnreadChat unreadChat) {
        return UnreadChatResponse.builder()
                .chatId(unreadChat.getChatId())
                .unreadUsername(unreadChat.getUnreadUsername())
                .sender(unreadChat.getSender())
                .content(unreadChat.getContent())
                .build();
    }
}
