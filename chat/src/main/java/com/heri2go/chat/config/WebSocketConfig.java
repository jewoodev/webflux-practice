package com.heri2go.chat.config;

import com.heri2go.chat.web.service.chat.ChatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;

import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public HandlerMapping webSocketMapping(WebSocketHandler chatWebSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws-connect", chatWebSocketHandler), -1);
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler(ChatService chatService) {
        return session -> session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(chatService::processMessage) // 비동기 처리
                .map(session::textMessage)
                .as(session::send);
    }
}