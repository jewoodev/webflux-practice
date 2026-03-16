package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.service.user.response.UserResponse;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "chat_room_participants")
public class ChatRoomParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String username;
    private Long chatRoomId;

    @Builder
    private ChatRoomParticipant(Long userId, String username, Long chatRoomId) {
        this.userId = userId;
        this.username = username;
        this.chatRoomId = chatRoomId;
    }

    public static ChatRoomParticipant from(UserResponse userResp, Long chatRoomId) {
        return ChatRoomParticipant.builder()
                .userId(userResp.id())
                .username(userResp.username())
                .chatRoomId(chatRoomId)
                .build();
    }
}
