package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.UnreadChatService;
import com.heri2go.chat.web.service.chat.response.UnreadChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RequestMapping("/api/unread-chat")
@RestController
public class UnreadChatController {

    private final UnreadChatService unreadChatService;

    @GetMapping("/info") // 자신에게 수신된 채팅 중에 읽지 않은 채팅들을 조회한다.
    public Flux<UnreadChatResponse> getOwnUnreadChat(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return unreadChatService.getOwnByUserDetails(userDetails);
    }
}
