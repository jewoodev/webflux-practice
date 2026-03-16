package com.heri2go.chat.util.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.exception.JsonConvertException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ChatConverter {
    private final ObjectMapper objectMapper;

    public ChatCreateRequest convertToReq(String messagePayload) {
        try {
            return objectMapper.readValue(messagePayload, ChatCreateRequest.class);
        } catch (JsonProcessingException e) {
            log.error("메세지 -> ChatMessageReq 변환 중 에러 발생: ", e);
            throw new JsonConvertException("메세지 -> ChatMessageReq 변환 중 에러 발생");
        }
    }

    public String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 중 에러 발생: ", e);
            throw new JsonConvertException("JSON 변환 중 에러 발생");
        }
    }
}
