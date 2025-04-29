package com.heri2go.chat.domain.chatroom;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document
public class ChatRoomParticipant {

    @Id
    private String id;
    private String username;
    private String chatRoomId;
    private LocalDateTime joinedAt;

    @Builder
    private ChatRoomParticipant(String username, String chatRoomId, LocalDateTime joinedAt) {
        this.username = username;
        this.chatRoomId = chatRoomId;
        this.joinedAt = joinedAt;
    }
}
