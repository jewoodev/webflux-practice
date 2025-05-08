package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import com.heri2go.chat.web.service.session.ConnectInfoProvider;
import com.heri2go.chat.web.service.session.RedisSessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.heri2go.chat.web.service.session.ConnectInfoProvider.SERVER_ID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final RedisSessionManager sessionManager;
    private final ChatConverter chatConverter;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ConnectInfoProvider cip;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSubscription() {
        redisTemplate.listenToPattern(cip.getRoomKey("*"))
                .doOnError(error -> log.error("Redis subscription error: ", error))
                .flatMap(message -> {
                    String channel = message.getChannel();
                    String roomId = channel.substring(SERVER_ID.length() + 1);
                    return broadcastToRoom(roomId, message.getMessage());
                })
                .subscribe();
    }

    @PreDestroy
    public void tearDown() {
        sessions.keySet().forEach(sessionManager::removeSession);
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getPrincipal)
                .cast(UserDetailsImpl.class)
                .flatMap(userDetails -> {
                    // 세션 저장
                    sessions.put(session.getId(), session);
                    
                    // 사용자의 모든 채팅방 구독
                    return chatRoomService.getOwnChatRoomResponse(userDetails)
                            .flatMap(chatRoom -> {
                                return sessionManager.saveSession(session.getId(), chatRoom.id(), userDetails.getUsername());
                            })
                            .then(handleMessage(session)); // 실시간 메세지 처리 방식 정의
                })
                .onErrorResume(e -> {
                    log.error("WebSocket connection error", e);
                    return session.close(CloseStatus.SERVER_ERROR.withReason("Internal server error"));
                });
    }

    private Mono<Void> handleMessage(WebSocketSession session) {
        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(payload -> handleIncomingMessage(session, payload))
                .then(Mono.fromRunnable(() -> {
                    sessions.remove(session.getId());
                    sessionManager.removeSession(session.getId()).subscribe();
                }));
    }

    private Mono<Void> handleIncomingMessage(WebSocketSession session, String payload) {
        return chatConverter.convertToReq(payload)
                .flatMap(chatMessage -> {
                    switch (chatMessage.type()) {
                        case ENTER:
                            return handleEnterMessage(session, chatMessage);
                        case LEAVE:
                            return handleLeaveMessage(session, chatMessage);
                        case TALK:
                            return handleChatMessage(chatMessage);
                        default:
                            return Mono.error(new IllegalArgumentException("Unknown content type"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error processing content: ", e);
                    return sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다.");
                });
    }

    private Mono<Void> handleEnterMessage(WebSocketSession session, ChatCreateRequest message) {
        return sessionManager.saveSession(session.getId(),
                message.roomId(),
                message.sender())
                .then(publishMessage(message));
    }

    private Mono<Void> handleLeaveMessage(WebSocketSession session, ChatCreateRequest message) {
        return sessionManager.removeSession(session.getId())
                .then(publishMessage(message));
    }

    private Mono<Void> handleChatMessage(ChatCreateRequest message) {
        return chatService.processMessage(message)
                .flatMap(jsonMessage -> publishMessage(message))
                .then();
    }

    private Mono<Void> publishMessage(ChatCreateRequest chatMessage) {
        return chatConverter.convertToJson(chatMessage)
                .flatMap(message -> redisTemplate.convertAndSend(
                        cip.getRoomKey(chatMessage.roomId()),
                        message))
                .then();
    }

    private Mono<Void> broadcastToRoom(String roomId, String message) {
        return sessionManager.getRoomSessions(roomId.toString())
                .flatMap(sessionId -> sendMessageToSession(sessionId, message))
                .then();
    }

    private Mono<Void> sendMessageToSession(String sessionId, String message) {
        return sessionManager.getSessionInfo(sessionId)
                .flatMap(sessionInfo -> {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        return session.send(Mono.just(session.textMessage(message)))
                                .onErrorResume(e -> {
                                    log.error("Failed to send content to session {}: ", sessionId, e);
                                    return sessionManager.removeSession(sessionId);
                                });
                    }
                    return sessionManager.removeSession(sessionId);
                })
                .then();
    }

    private Mono<Void> sendErrorMessage(WebSocketSession session, String errorMessage) {
        return session.send(Mono.just(session.textMessage(errorMessage)))
                .onErrorResume(e -> {
                    log.error("Error sending error content: ", e);
                    return Mono.empty();
                });
    }
}
