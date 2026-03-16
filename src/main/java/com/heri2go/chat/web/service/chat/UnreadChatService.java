package com.heri2go.chat.web.service.chat;

import com.heri2go.chat.domain.RedisDao;
import com.heri2go.chat.domain.chat.Chat;
import com.heri2go.chat.domain.chat.UnreadChat;
import com.heri2go.chat.domain.chat.UnreadChatRepository;
import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import com.heri2go.chat.web.service.chat.response.UnreadChatResponse;
import com.heri2go.chat.web.service.session.ConnectInfoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class UnreadChatService {

    private final UnreadChatRepository unreadChatRepository;
    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    public Mono<ChatResponse> save(Chat chat) {
        return unreadChatRepository.saveAll(UnreadChat.from(chat))
                .then(Mono.just(ChatResponse.from(chat)));
    }

    public Flux<UnreadChatResponse> getOwnByUserDetails(UserDetailsImpl userDetails) {
        return unreadChatRepository.findAllByUnreadUsername(userDetails.getUsername())
                .map(UnreadChatResponse::from);
    }

    public Flux<UnreadChatResponse> getOfflineChat(UserDetailsImpl userDetails) {
        String lastOnlineTimeKey = cip.getLastOnlineTimeKey(userDetails.getUsername());
        return redisDao.getLastOnlineTime(lastOnlineTimeKey)
                .flatMapMany(lastOnlineTime ->
                        unreadChatRepository.findAllByUnreadUsername(userDetails.getUsername())
                                .filter(unreadChat -> unreadChat.getCreatedAt().isAfter(lastOnlineTime))
                                .map(UnreadChatResponse::from)
                );
    }
}
