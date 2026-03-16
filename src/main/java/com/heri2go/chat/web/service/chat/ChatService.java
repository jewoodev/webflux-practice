package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.exception.MessageInvalidException;
import com.heri2go.chat.web.exception.ResourceNotFoundException;
import com.heri2go.chat.web.exception.UnauthorizedException;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatConverter chatConverter;
    private final UnreadChatService unreadChatService;
    private final ChatRoomService chatRoomService;

    @Transactional
    public Chat save(ChatCreateRequest req) {
        Chat chat = chatRepository.save(Chat.from(req));
        log.info("채팅 저장 성공");
        return chat;
    }

    @Transactional(readOnly = true)
    public List<ChatResponse> getByRoomIdToInvited(Long roomId, UserDetailsImpl userDetails) {
        Set<Long> participantIds = chatRoomService.getParticipantIdsById(roomId);
        if (participantIds == null) {
            throw new ResourceNotFoundException("존재하지 않는 채팅방으로의 접근입니다.");
        }
        if (!participantIds.contains(userDetails.getUserId())) {
            throw new UnauthorizedException("접근 권한이 없는 채팅방입니다.");
        }
        return chatRepository.findByRoomId(roomId).stream()
                .map(ChatResponse::from)
                .toList();
    }

    @Transactional
    public String processMessage(ChatCreateRequest req) {
        if (req.content() == null || req.content().isEmpty()) {
            log.error("메세지 내용이 비어있어 에러 발생");
            throw new MessageInvalidException("메세지 내용이 비어있어 에러 발생");
        }

        Chat chat = this.save(req);
        log.info("채팅 발생 -> 채팅방 정보 갱신: 채팅 메세지 = {}", chat.getContent());
        chatRoomService.updateAboutLastChat(chat);
        ChatResponse chatResponse = unreadChatService.save(chat);
        return chatConverter.convertToJson(chatResponse);
    }
}
