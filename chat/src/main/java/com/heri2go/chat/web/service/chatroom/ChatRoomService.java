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
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ReactiveMongoTemplate mongoTemplate;
    private final ChatRoomParticipantService chatRoomParticipantService;
    private final UserService userService;
    private final RedisSessionManager sessionManager;
    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    public Mono<ChatRoomResponse> save(ChatRoomCreateRequest request) {
        return validateParticipants(request)
                .flatMap(userResponses -> createChatRoom(request)
                        .flatMap(chatRoom -> saveParticipants(chatRoom, userResponses))
                )
                .flatMap(chatRoom -> updateSessionInfo(chatRoom))
                .map(ChatRoomResponse::from);
    }

    private Mono<List<UserResponse>> validateParticipants(ChatRoomCreateRequest request) {
        return Flux.fromIterable(request.participantIds())
                .flatMap(userService::getById)
                .collectList()
                .flatMap(users -> {
                    if (users.size() != request.participantIds().size()) {
                        return Mono.error(new UserNotFoundException("One or more participants not found"));
                    }
                    return Mono.just(users);
                });
    }

    private Mono<ChatRoom> createChatRoom(ChatRoomCreateRequest request) {
        return chatRoomRepository.save(ChatRoom.from(request));
    }

    private Mono<ChatRoom> saveParticipants(ChatRoom chatRoom, List<UserResponse> userResponses) {
        return Flux.fromIterable(userResponses)
                .map(userResponse -> ChatRoomParticipant.from(userResponse, chatRoom.getId()))
                .collectList()
                .flatMap(crp -> chatRoomParticipantRepository.saveAll(crp)
                        .then(Mono.just(chatRoom)));
    }

    private Mono<ChatRoom> updateSessionInfo(ChatRoom chatRoom) {
        return Flux.fromIterable(chatRoom.getParticipantIds())
                .flatMap(participantId -> {
                    String sessionIdKey = cip.getSessionIdKey(participantId);
                    return redisDao.getString(sessionIdKey)
                            .flatMap(sessionId ->
                                sessionManager.saveRoomSession(sessionId, chatRoom.getId(), participantId)
                            );
                })
                .then(Mono.just(chatRoom));
    }

    @Cacheable(value = "ChatPI", key = "#p0", cacheManager = "cacheManager", unless = "#result == null")
    public Mono<Set<String>> getParticipantIdsById(String id) {
        return chatRoomRepository.findById(id)
                .map(ChatRoom::getParticipantIds);
    }

    public Flux<ChatRoomResponse> getOwnChatRoomResponse(UserDetailsImpl userDetails) {
        return chatRoomParticipantService.getAllByUserId(userDetails.getUserId())
                .flatMap(chatRoomParticipant ->
                        chatRoomRepository.findById(chatRoomParticipant.getChatRoomId())
                .map(ChatRoomResponse::from))
                .switchIfEmpty(Mono.error(new ChatRoomNotFoundException("참여 중인 채팅방이 없습니다.")));
    }

    public Mono<Chat> updateAboutLastChat(Chat chat) {
        return mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(chat.getRoomId())),
                new Update()
                        .set("lastMessage", chat.getOriginalContent())
                        .set("lastSender", chat.getSender())
                        .set("lastMessageTime", chat.getCreatedAt())
                        .set("updatedAt", chat.getCreatedAt()),
                ChatRoom.class
        ).then(Mono.just(chat));
    }
}
