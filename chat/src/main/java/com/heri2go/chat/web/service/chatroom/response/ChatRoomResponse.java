package com.heri2go.chat.web.service.chatroom.response;

import com.heri2go.chat.domain.chatroom.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ChatRoomResponse(
        String id,
        String orderId,
        String roomName,
        Set<String> participantIds,
        String lastMessage,
        String lastSender,
        LocalDateTime lastMessageTime
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .orderId(chatRoom.getOrderId())
                .roomName(chatRoom.getRoomName())
                .participantIds(chatRoom.getParticipantIds())
                .lastMessage(chatRoom.getLastMessage())
                .lastSender(chatRoom.getLastSender())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .build();
    }
}
