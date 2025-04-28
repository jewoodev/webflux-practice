package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public Flux<ChatRoomResponse> getOwnChatRoomResponse(UserDetailsImpl userDetails) {
        return null;
    }
}
