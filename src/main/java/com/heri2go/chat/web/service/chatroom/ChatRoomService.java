package com.heri2go.chat.web.service.chatroom;

import com.heri2go.chat.domain.RedisDao;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chatroom.ChatRoom;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipant;
import com.heri2go.chat.domain.chatroom.ChatRoomParticipantRepository;
import com.heri2go.chat.domain.chatroom.ChatRoomRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.exception.ChatRoomNotFoundException;
import com.heri2go.chat.web.exception.UserNotFoundException;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import com.heri2go.chat.web.service.session.ConnectInfoProvider;
import com.heri2go.chat.web.service.session.RedisSessionManager;
import com.heri2go.chat.web.service.user.UserService;
import com.heri2go.chat.web.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final UserService userService;
    private final RedisSessionManager sessionManager;
    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    @Transactional
    public ChatRoomResponse save(ChatRoomCreateRequest request) {
        List<UserResponse> userResponses = validateParticipants(request);
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.from(request));
        saveParticipants(chatRoom, userResponses);
        updateSessionInfo(chatRoom);
        return ChatRoomResponse.from(chatRoom);
    }

    private List<UserResponse> validateParticipants(ChatRoomCreateRequest request) {
        List<UserResponse> users = request.participantIds().stream()
                .map(userService::getById)
                .toList();
        if (users.size() != request.participantIds().size()) {
            throw new UserNotFoundException("One or more participants not found");
        }
        return users;
    }

    private void saveParticipants(ChatRoom chatRoom, List<UserResponse> userResponses) {
        List<ChatRoomParticipant> participants = userResponses.stream()
                .map(userResponse -> ChatRoomParticipant.from(userResponse, chatRoom.getId()))
                .toList();
        chatRoomParticipantRepository.saveAll(participants);
    }

    private void updateSessionInfo(ChatRoom chatRoom) {
        for (Long participantId : chatRoom.getParticipantIds()) {
            String sessionIdKey = cip.getSessionIdKey(String.valueOf(participantId));
            String sessionId = redisDao.getString(sessionIdKey);
            if (sessionId != null) {
                sessionManager.saveRoomSession(sessionId, String.valueOf(chatRoom.getId()), String.valueOf(participantId));
            }
        }
    }

    @Cacheable(value = "ChatPI", key = "#p0", cacheManager = "cacheManager", unless = "#result == null")
    public Set<Long> getParticipantIdsById(Long id) {
        return chatRoomRepository.findById(id)
                .map(ChatRoom::getParticipantIds)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getOwnChatRoomResponse(UserDetailsImpl userDetails) {
        List<ChatRoomParticipant> participants = chatRoomParticipantService.getAllByUserId(userDetails.getUserId());
        if (participants.isEmpty()) {
            throw new ChatRoomNotFoundException("참여 중인 채팅방이 없습니다.");
        }
        return participants.stream()
                .map(participant -> chatRoomRepository.findById(participant.getChatRoomId())
                        .map(ChatRoomResponse::from)
                        .orElse(null))
                .filter(response -> response != null)
                .toList();
    }

    @Transactional
    public void updateAboutLastChat(Chat chat) {
        chatRoomRepository.findById(chat.getRoomId())
                .ifPresent(chatRoom -> {
                    chatRoom.updateLastChat(chat.getContent(), chat.getSender(), chat.getCreatedAt());
                    chatRoomRepository.save(chatRoom);
                });
    }
}
