package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.UnreadChat;
import com.heri2go.chat.domain.chat.UnreadChatRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.chat.response.UnreadChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UnreadChatService {

    private final UnreadChatRepository unreadChatRepository;

    public Mono<ChatResponse> save(Chat chat) {
        return unreadChatRepository.saveAll(UnreadChat.from(chat))
                .then(Mono.just(ChatResponse.from(chat)));
    }

    public Flux<UnreadChatResponse> getOwnByUserDetails(UserDetailsImpl userDetails) {
        return unreadChatRepository.findByUnreadUsername(userDetails.getUsername())
                .map(UnreadChatResponse::from);
    }
}
