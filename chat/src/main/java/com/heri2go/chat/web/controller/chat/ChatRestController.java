package com.heri2go.chat.web.controller.chat;

import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
public class ChatRestController {

    private final ChatService chatService;

    @GetMapping("/chat/{roomNum}")
    public Flux<ChatResponse> getChatHistory(@PathVariable("roomNum") Long roomNum) {
        return chatService.getByRoomNum(roomNum);
    }
}
