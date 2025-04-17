package com.heri2go.chat.config;

import com.heri2go.chat.web.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ChatService chatService;
    private final DefaultMessageDelegate messageDelegate;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("WebSocket 연결: 세션 ID={}", session.getId());

        // Redis에서 받은 메시지를 WebSocket으로 스트리밍
        Mono<Void> outgoingMessages = session.send(
                messageDelegate.getMessageStream()
                        .map(session::textMessage)
        );

        // 클라이언트에서 받은 메시지를 처리 (등록/번역/Redis 발행)
        Mono<Void> incomingMessages = session.receive()
                .map(message -> message.getPayloadAsText())
                .flatMap(chatService::processIncomingMessage) // 서비스에 메시지 위임
                .doOnNext(processed -> log.info("클라이언트 메시지 처리 완료"))
                .then();

        return Mono.zip(outgoingMessages, incomingMessages).then();
    }
}

