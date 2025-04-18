package com.heri2go.chat.util.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heri2go.chat.domain.chat.dto.ChatMessageReq;
import com.heri2go.chat.web.exception.JsonConvertException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatConverter {
    private final ObjectMapper objectMapper;

    public Mono<ChatMessageReq> convertToReq(String messagePayload) {
        return Mono.fromCallable(() -> objectMapper.readValue(messagePayload, ChatMessageReq.class))
                .onErrorResume(e -> {
                    log.error("메세지 -> ChatMessageReq 변환 중 에러 발생: ", e);
                    return Mono.error(new JsonConvertException("메세지 -> ChatMessageReq 변환 중 에러 발생"));
                });
    }

    public Mono<String> convertToJson(Object object) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(object))
                .onErrorMap(JsonProcessingException.class, e -> {
                    log.error("JSON 변환 중 에러 발생: ", e);
                    return new JsonConvertException("JSON 변환 중 에러 발생");
                }); // 예외를 리액티브 스트림 안에서 처리
    }
}
