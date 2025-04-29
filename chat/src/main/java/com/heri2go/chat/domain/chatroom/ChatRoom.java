package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@Document
public class ChatRoom {

    @Id
    private String id;
    private String roomName;
    private Set<String> participantIds;
    private String lastMessage;
    private String lastSender;
    private LocalDateTime lastMessageTime;

    @Builder
    private ChatRoom(String roomName, Set<String> participantIds, String lastMessage, String lastSender, LocalDateTime lastMessageTime) {
        this.roomName = roomName;
        this.participantIds = participantIds;
        this.lastMessage = lastMessage;
        this.lastSender = lastSender;
        this.lastMessageTime = lastMessageTime;
    }

    public static ChatRoom from(ChatRoomCreateRequest request) {
        return ChatRoom.builder()
                .roomName(request.roomName())
                .participantIds(request.participantIds())
                .lastMessage(request.lastMessage())
                .lastSender(request.lastSender())
                .lastMessageTime(request.lastMessageTime())
                .build();
    }
}
