package com.heri2go.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;

@Configuration
public class WebSocketConfig {
    @Bean
    public HandlerMapping webSocketMapping(WebSocketHandler chatWebSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws-connect", chatWebSocketHandler), -1);
    }
}