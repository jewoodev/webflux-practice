package com.heri2go.chat.domain.chatroom;

import com.heri2go.chat.domain.BaseTimeEntity;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document
public class ChatRoomParticipant extends BaseTimeEntity {

    @Id
    private String id;

    @Indexed
    private String userId;
    private String username;
    private String chatRoomId;

    @Builder
    private ChatRoomParticipant(String userId, String username, String chatRoomId) {
        this.userId = userId;
        this.username = username;
        this.chatRoomId = chatRoomId;
    }

    public static ChatRoomParticipant from(UserResponse userResp, String chatRoomId) {
        return ChatRoomParticipant.builder()
                .userId(userResp.id())
                .username(userResp.username())
                .chatRoomId(chatRoomId)
                .build();
    }
}
