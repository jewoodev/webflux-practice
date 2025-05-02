package com.heri2go.chat.web.service.chat;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.ChatRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.util.chat.ChatConverter;
import com.heri2go.chat.web.controller.chat.request.ChatCreateRequest;
import com.heri2go.chat.web.exception.JsonConvertException;
import com.heri2go.chat.web.exception.MessageInvalidException;
import com.heri2go.chat.web.exception.ResourceNotFoundException;
import com.heri2go.chat.web.exception.UnauthorizedException;
import com.heri2go.chat.web.service.chat.response.ChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatConverter chatConverter;

    public Mono<ChatResponse> save(ChatCreateRequest req) {
        return chatRepository.save(Chat.from(req))
                .map(ChatResponse::fromEntity);
    }

    public Flux<ChatResponse> getByRoomIdToInvited(String roomId, UserDetailsImpl userDetails) {
        return chatRepository.findByRoomId(roomId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("존재하지 않는 채팅방입니다.")))
                .filter(chat -> chat.getUnreadUsernames().contains(userDetails.getUsername()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("접근 권한이 없는 채팅방입니다.")))
                .map(ChatResponse::fromEntity);
    }

    public Mono<String> processMessage(ChatCreateRequest req) {
        return Mono.defer(() -> {
            if (req.content() == null || req.content().isEmpty()) {
                log.error("메세지 내용이 비어있어 에러 발생");
                return Mono.error(new MessageInvalidException("메세지 내용이 비어있어 에러 발생"));
            }

            return this.save(req)
                    .flatMap(chatConverter::convertToJson)
                    .onErrorResume(JsonProcessingException.class, e -> {
                        log.error("메세지 저장 후 응답 Dto로 변환 중 에러 발생: ", e);
                        return Mono.error(new JsonConvertException("메세지 저장 후 응답 Dto로 변환 중 에러 발생"));
                    });
        });
    }
}
