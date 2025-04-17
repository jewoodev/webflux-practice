package com.heri2go.chat.web.service.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.chat.dto.ChatMessageReq;
import com.heri2go.chat.domain.chat.dto.ChatMessageResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final TranslateService translateService;
    private final ObjectMapper objectMapper;
    private final ReactiveRedisOperations<String, String> mainRedisOperations;

    public Mono<ChatMessageResp> save(ChatMessageReq req) {
        return chatRepository.save(Chat.fromReq(req))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ChatMessageResp::fromEntity);
    }

    public Flux<ChatMessageResp> getByRoomNum(Long roomNum) {
        return chatRepository.findByRoomNumOrderByCreatedAt(roomNum)
                .subscribeOn(Schedulers.boundedElastic())
                .map(ChatMessageResp::fromEntity);
    }

    public Mono<Void> processIncomingMessage(String payload) {
        return Mono.fromCallable(() -> objectMapper.readTree(payload)) // 비동기적으로 처리
                .map(jsonNode -> objectMapper.convertValue(jsonNode, ChatMessageReq.class)) // 변환
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(req -> {
                    String targetLang = req.getLang().equals("ko") ? "en" : "ko";
                    return translateService.translate(req.getMsg(), req.getLang(), targetLang)
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(translatedMsg -> {
                                req.setMsgAfterTranslate(translatedMsg);
                                try {
                                    return mainRedisOperations.convertAndSend("chat:room:" + req.getRoomNum(),
                                            objectMapper.writeValueAsString(req));
                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                            }).then(save(req));
                })
                .onErrorResume(error -> {
                    log.error("메시지 처리 실패", error);
                    return Mono.empty();
                }).then();
    }

}
