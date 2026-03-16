package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Service
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public Flux<ChatRoomParticipant> getAllByUserId(String userId) {
        return chatRoomParticipantRepository.findAllByUserId(userId); // 참여 중인 채팅방이 없는 것은 오류가 아니기 때문에 결과가 Empty인 경우를 핸들링 x
    }
}
