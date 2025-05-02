package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RequestMapping("/api/chat")
@RestController
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{roomId}") // 채팅 기록을 가져올 때 채팅방에 초대되어 있는 유저가 아니면 실패한다.
    public Flux<ChatResponse> getChatHistory(@PathVariable("roomId") String roomId,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return chatService.getByRoomIdToInvited(roomId, userDetails);
    }
}
