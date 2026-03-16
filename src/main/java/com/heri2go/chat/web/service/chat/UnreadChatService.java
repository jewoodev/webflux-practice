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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UnreadChatService {

    private final UnreadChatRepository unreadChatRepository;
    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    @Transactional
    public ChatResponse save(Chat chat) {
        unreadChatRepository.saveAll(UnreadChat.from(chat));
        return ChatResponse.from(chat);
    }

    @Transactional(readOnly = true)
    public List<UnreadChatResponse> getOwnByUserDetails(UserDetailsImpl userDetails) {
        return unreadChatRepository.findAllByUnreadUsername(userDetails.getUsername()).stream()
                .map(UnreadChatResponse::from)
                .toList();
    }

    public List<UnreadChatResponse> getOfflineChat(UserDetailsImpl userDetails) {
        String lastOnlineTimeKey = cip.getLastOnlineTimeKey(userDetails.getUsername());
        LocalDateTime lastOnlineTime = redisDao.getLastOnlineTime(lastOnlineTimeKey);
        return unreadChatRepository.findAllByUnreadUsername(userDetails.getUsername()).stream()
                .filter(unreadChat -> unreadChat.getCreatedAt().isAfter(lastOnlineTime))
                .map(UnreadChatResponse::from)
                .toList();
    }
}
