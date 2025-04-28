package com.heri2go.chat.web.controller.chatroom;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/info")
    public Flux<ChatRoomResponse> getOwnChatRoomResponse(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatRoomService.getOwnChatRoomResponse(userDetails);
    }
}
