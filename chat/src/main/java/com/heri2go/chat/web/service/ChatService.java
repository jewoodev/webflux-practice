package com.heri2go.chat.web.service;

import com.heri2go.chat.domain.Chat;
import com.heri2go.chat.domain.ChatRepository;
import com.heri2go.chat.domain.dto.ChatMessageReq;
import com.heri2go.chat.domain.dto.ChatMessageResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;

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
}
