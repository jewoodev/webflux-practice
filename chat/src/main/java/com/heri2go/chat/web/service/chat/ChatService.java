package com.heri2go.chat.web.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.chat.dto.ChatMessageReq;
import com.heri2go.chat.domain.chat.dto.ChatMessageResp;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.exception.JsonConvertException;
import com.heri2go.chat.web.exception.MessageInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatConverter chatConverter;

    public Mono<ChatMessageResp> save(ChatMessageReq req) {
        return chatRepository.save(Chat.fromReq(req))
                .map(ChatMessageResp::fromEntity);
    }

    public Flux<ChatMessageResp> getByRoomNum(Long roomNum) {
        return chatRepository.findByRoomNumOrderByCreatedAt(roomNum)
                .map(ChatMessageResp::fromEntity);
    }

    public Mono<String> processMessage(String messagePayload) {
        return chatConverter.convertToReq(messagePayload)
                .flatMap(chatMessageReq -> {
                    if (chatMessageReq.getMsg() == null || chatMessageReq.getMsg().isEmpty()) {
                        log.error("메세지 내용이 비어있어 에러 발생");
                        return Mono.error(new MessageInvalidException("메세지 내용이 비어있어 에러 발생"));
                    }

                    // 비즈니스 로직: 메시지 저장
                    return save(chatMessageReq)
                            .flatMap(chatMessageResp -> chatConverter.convertToJson(chatMessageResp))
                            .onErrorResume(JsonProcessingException.class, e -> {
                                log.error("메세지 저장 후 응답 Dto로 변환 중 에러 발생: ", e);
                                return Mono.error(new JsonConvertException("메세지 저장 후 응답 Dto로 변환 중 에러 발생"));
                            });
                });
    }
}
