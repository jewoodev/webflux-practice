package com.heri2go.chat.web.controller.chatroom;

import com.heri2go.chat.domain.user.UserDetailsImpl;
import com.heri2go.chat.web.controller.chatroom.request.ChatRoomCreateRequest;
import com.heri2go.chat.web.service.chatroom.ChatRoomService;
import com.heri2go.chat.web.service.chatroom.response.ChatRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/api/chat-room")
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping("/info") // 자신이 참여 중인 채팅방들의 정보를 조회한다.
    public Flux<ChatRoomResponse> getOwnChatRoomInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return chatRoomService.getOwnChatRoomResponse(userDetails);
    }

    @PostMapping("/create") // 주문이 생성되면 채팅방이 생성된다.
    public Mono<ChatRoomResponse> createChatRoom(@RequestBody ChatRoomCreateRequest request) {
        return chatRoomService.save(request);
    }
}
