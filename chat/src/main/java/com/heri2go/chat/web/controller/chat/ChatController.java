package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.domain.chat.dto.ChatMessageResp;
import com.heri2go.chat.web.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat/{roomNum}")
    public Flux<ChatMessageResp> getChatHistory(@PathVariable Long roomNum) {
        return chatService.getByRoomNum(roomNum);
    }

    @GetMapping("/test-session")
    public Mono<String> checkSession(ServerWebExchange exchange) {
        return exchange.getSession()
                .map(session -> "세션 ID: " + session.getId() + ", 속성: " + session.getAttributes());
    }
}
