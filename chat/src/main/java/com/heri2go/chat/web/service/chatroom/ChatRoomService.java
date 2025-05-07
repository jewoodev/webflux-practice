package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.exception.UserNotFoundException;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import com.heri2go.chat.web.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserService userService;

    public Mono<ChatRoomResponse> save(ChatRoomCreateRequest request) {
        return Flux.fromIterable(request.participantIds())
                .flatMap(userId -> userService.getById(userId))
                .collectList()
                .flatMap(users -> {
                    if (users.size() != request.participantIds().size()) {
                        return Mono.error(new UserNotFoundException("One or more participants not found"));
                    }
                    return chatRoomRepository.save(ChatRoom.from(request))
                            .map(ChatRoomResponse::from);
                });
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
