package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.domain.RedisDao;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.exception.UnauthorizedException;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import com.heri2go.chat.web.service.session.ConnectInfoProvider;
import com.heri2go.chat.web.service.session.RedisSessionManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final UnreadChatService unreadChatService;
    private final RedisSessionManager sessionManager;
    private final ChatConverter chatConverter;
    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;
    private final RedisMessageListenerContainer listenerContainer;

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @PostConstruct
    public void initSubscription() {
        listenerContainer.addMessageListener(
                (Message message, byte[] pattern) -> {
                    String channel = new String(message.getChannel());
                    String body = new String(message.getBody());
                    broadcastToRoom(channel, body);
                },
                new PatternTopic(cip.getRoomSessionsKey("*"))
        );
    }

    @PreDestroy
    public void tearDown() {
        sessions.keySet().forEach(sessionId -> sessionManager.removeRoomSession(sessionId, "*"));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            UserDetailsImpl userDetails = getUserDetails(session);
            if (userDetails == null) {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthorized"));
                return;
            }

            sessions.put(session.getId(), session);

            // 사용자의 모든 채팅방 구독
            try {
                chatRoomService.getOwnChatRoomResponse(userDetails).forEach(chatRoom -> {
                    sessionManager.saveRoomSession(session.getId(), String.valueOf(chatRoom.id()), String.valueOf(userDetails.getUserId()));

                    // 오프라인 상태인 동안 처리되지 못한 메세지 알림
                    unreadChatService.getOfflineChat(userDetails).forEach(unreadChat -> {
                        String json = chatConverter.convertToJson(unreadChat);
                        sendMessageToSession(session.getId(), json);
                    });
                });
            } catch (Exception e) {
                log.debug("No chat rooms for user: {}", userDetails.getUsername());
            }
        } catch (Exception e) {
            log.error("WebSocket connection error", e);
            try {
                session.close(CloseStatus.SERVER_ERROR.withReason("Internal server error"));
            } catch (IOException ex) {
                log.error("Error closing session", ex);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            UserDetailsImpl userDetails = getUserDetails(session);
            if (userDetails == null) return;

            String payload = message.getPayload();
            handleIncomingMessage(session, payload, String.valueOf(userDetails.getUserId()));
        } catch (Exception e) {
            log.error("Unexpected error processing message: ", e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        UserDetailsImpl userDetails = getUserDetails(session);
        sessions.remove(session.getId());
        if (userDetails != null) {
            sessionManager.removeRoomSession(session.getId(), String.valueOf(userDetails.getUserId()));
            redisDao.setString(
                    cip.getLastOnlineTimeKey(userDetails.getUsername()),
                    LocalDateTime.now().toString()
            );
            redisDao.expire(cip.getLastOnlineTimeKey(userDetails.getUsername()), Duration.ofDays(7));
        }
    }

    private void handleIncomingMessage(WebSocketSession session, String payload, String userId) {
        ChatCreateRequest chatMessage = chatConverter.convertToReq(payload);
        if (chatMessage == null) return;

        Set<Long> participantIds = chatRoomService.getParticipantIdsById(chatMessage.roomId());
        if (participantIds == null || !participantIds.contains(Long.valueOf(userId))) {
            sendErrorMessage(session, "참여 중이지 않은 채팅방으로 채팅을 보낼 수 없습니다.");
            return;
        }

        try {
            switch (chatMessage.type()) {
                case ENTER -> handleEnterMessage(chatMessage);
                case LEAVE -> handleLeaveMessage(session, chatMessage, userId);
                case TALK -> handleChatMessage(chatMessage);
                default -> throw new IllegalArgumentException("Unknown content type in Chat");
            }
        } catch (UnauthorizedException e) {
            sendErrorMessage(session, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error processing content: ", e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    private void handleEnterMessage(ChatCreateRequest message) {
        publishMessage(message);
    }

    private void handleLeaveMessage(WebSocketSession session, ChatCreateRequest message, String userId) {
        sessionManager.removeRoomSession(session.getId(), userId);
        publishMessage(message);
    }

    private void handleChatMessage(ChatCreateRequest message) {
        chatService.processMessage(message);
        publishMessage(message);
    }

    private void publishMessage(ChatCreateRequest chatMessage) {
        String json = chatConverter.convertToJson(chatMessage);
        redisDao.convertAndSend(cip.getRoomSessionsKey(String.valueOf(chatMessage.roomId())), json);
    }

    private void broadcastToRoom(String roomKey, String message) {
        Set<String> sessionIds = sessionManager.getRoomSessionIds(roomKey);
        if (sessionIds != null) {
            sessionIds.forEach(sessionId -> sendMessageToSession(sessionId, message));
        }
    }

    private void sendMessageToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                log.error("Failed to send content to session {}: ", sessionId, e);
            }
        }
    }

    private void sendErrorMessage(WebSocketSession session, String errorMessage) {
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(errorMessage));
            }
        } catch (IOException e) {
            log.error("Error sending error content: ", e);
        }
    }

    private UserDetailsImpl getUserDetails(WebSocketSession session) {
        if (session.getPrincipal() instanceof Authentication auth) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetails) {
                return userDetails;
            }
        }
        return null;
    }
}
