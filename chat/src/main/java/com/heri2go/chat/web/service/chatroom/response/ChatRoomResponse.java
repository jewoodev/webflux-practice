package com.heri2go.chat.web.service.chatroom.response;

import com.heri2go.chat.domain.chatroom.ChatRoom;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
public record ChatRoomResponse(
        String roomName,
        Set<String> participantIds,
        String lastMessage,
        String lastSender,
        LocalDateTime lastMessageTime
) {
    public static ChatRoomResponse fromEntity(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .roomName(chatRoom.getRoomName())
                .participantIds(chatRoom.getParticipantIds())
                .lastMessage(chatRoom.getLastMessage())
                .lastSender(chatRoom.getLastSender())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .build();
    }
}
