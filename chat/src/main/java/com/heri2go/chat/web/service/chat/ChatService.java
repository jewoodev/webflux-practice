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
    private final UnreadChatService unreadChatService;

    public Mono<Chat> save(ChatCreateRequest req) {
        return chatRepository.save(Chat.from(req));
    }

    public Flux<ChatResponse> getByRoomIdToInvited(String roomId, UserDetailsImpl userDetails) {
        return chatRepository.findByRoomId(roomId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("존재하지 않는 채팅방입니다.")))
                .filter(chat -> chat.getUnreadUsernames().contains(userDetails.getUsername()))
                .switchIfEmpty(Mono.error(new UnauthorizedException("접근 권한이 없는 채팅방입니다.")))
                .map(ChatResponse::from);
    }

    public Mono<String> processMessage(ChatCreateRequest req) {
        return Mono.defer(() -> {
            if (req.content() == null || req.content().isEmpty()) {
                log.error("메세지 내용이 비어있어 에러 발생");
                return Mono.error(new MessageInvalidException("메세지 내용이 비어있어 에러 발생"));
            }

            // 모든 채팅은 저장된 후, UnreadChat(단일 유저에게 수신된 메세지 중에 확인하지 않은 메세지 정보)를 추가적으로 저장해야 한다.
            return this.save(req)
                    .flatMap(unreadChatService::save)
                    .flatMap(chatConverter::convertToJson)
                    .onErrorResume(JsonProcessingException.class, e -> {
                        log.error("메세지 저장 후 응답 Dto로 변환 중 에러 발생: ", e);
                        return Mono.error(new JsonConvertException("메세지 저장 후 응답 Dto로 변환 중 에러 발생"));
                    });
        });
    }
}
