package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.service.session.RedisSessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.heri2go.chat.web.service.session.SessionKey.ROOM_KEY_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatWebSocketHandler implements WebSocketHandler {
    private final ChatService chatService;
    private final RedisSessionManager sessionManager;
    private final ChatConverter chatConverter;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSubscription() {
        redisTemplate.listenToPattern(ROOM_KEY_PREFIX + "*")
                .doOnError(error -> log.error("Redis subscription error: ", error))
                .flatMap(message -> {
                    String channel = message.getChannel();
                    Long roomNum = Long.valueOf(channel.substring(ROOM_KEY_PREFIX.length()));
                    return broadcastToRoom(roomNum, message.getMessage());
                })
                .subscribe();
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessions.put(session.getId(), session);
        
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
                    switch (chatMessage.getType()) {
                        case ENTER:
                            return handleEnterMessage(session, chatMessage);
                        case LEAVE:
                            return handleLeaveMessage(session, chatMessage);
                        case TALK:
                            return handleChatMessage(chatMessage);
                        default:
                            return Mono.error(new IllegalArgumentException("Unknown message type"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error processing message: ", e);
                    return sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다.");
                });
    }

    private Mono<Void> handleEnterMessage(WebSocketSession session, ChatCreateRequest message) {
        return sessionManager.saveSession(session.getId(), 
                                        message.getRoomNum().toString(), 
                                        message.getSender())
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
                        ROOM_KEY_PREFIX + chatMessage.getRoomNum(), 
                        message))
                .then();
    }

    private Mono<Void> broadcastToRoom(Long roomNum, String message) {
        return sessionManager.getRoomSessions(roomNum.toString())
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
                                    log.error("Failed to send message to session {}: ", sessionId, e);
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
                    log.error("Error sending error message: ", e);
                    return Mono.empty();
                });
    }
}
