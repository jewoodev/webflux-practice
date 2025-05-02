package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public Mono<ChatRoomResponse> save(ChatRoomCreateRequest request) {
        return chatRoomRepository.save(ChatRoom.from(request))
                .map(ChatRoomResponse::from);
    }

    public Mono<ChatRoom> getById(String roomId) {
        return chatRoomRepository.findById(roomId);
    }

    public Flux<ChatRoomResponse> getOwnChatRoomResponse(UserDetailsImpl userDetails) {
        return chatRoomParticipantRepository.findAllByUsername(userDetails.getUsername())
                .flatMap(chatRoomParticipant ->
                        chatRoomRepository.findById(chatRoomParticipant.getChatRoomId())
                .map(ChatRoomResponse::from));
    }
}
