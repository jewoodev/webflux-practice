package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat_rooms")
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private String orderId;
    private String roomName;

    @ElementCollection
    @CollectionTable(name = "chat_room_participant_ids", joinColumns = @JoinColumn(name = "chat_room_id"))
    @Column(name = "participant_id")
    private Set<Long> participantIds;

    private String lastMessage;
    private String lastSender;
    private LocalDateTime lastMessageTime;

    @Builder
    private ChatRoom(String orderId, String roomName, Set<Long> participantIds, String lastMessage, String lastSender) {
        this.orderId = orderId;
        this.roomName = roomName;
        this.participantIds = participantIds;
        this.lastMessage = lastMessage;
        this.lastSender = lastSender;
    }

    public static ChatRoom from(ChatRoomCreateRequest request) {
        return ChatRoom.builder()
                .orderId(request.orderId())
                .roomName(request.roomName())
                .participantIds(request.participantIds())
                .lastMessage(request.lastMessage())
                .lastSender(request.lastSender())
                .build();
    }

    public void updateLastChat(String lastMessage, String lastSender, LocalDateTime lastMessageTime) {
        this.lastMessage = lastMessage;
        this.lastSender = lastSender;
        this.lastMessageTime = lastMessageTime;
        this.updatedAt = lastMessageTime;
    }
}
