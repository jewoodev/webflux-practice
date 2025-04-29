package com.heri2go.chat.web.controller.chatroom.request;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ChatRoomCreateRequest(
        String roomName,
        Set<String> participantIds,
        String lastMessage,
        String lastSender,
        LocalDateTime lastMessageTime
) {
}
