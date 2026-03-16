package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatRoomParticipantService {

    private final ChatRoomParticipantRepository chatRoomParticipantRepository;

    public List<ChatRoomParticipant> getAllByUserId(Long userId) {
        return chatRoomParticipantRepository.findAllByUserId(userId);
    }
}
