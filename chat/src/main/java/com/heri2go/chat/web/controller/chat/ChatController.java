package com.heri2go.chat.web.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.heri2go.chat.web.service.chat.ChatService;
import com.heri2go.chat.web.service.chat.response.ChatResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/chat/{roomNum}")
    public Flux<ChatResponse> getChatHistory(@PathVariable("roomNum") Long roomNum) {
        return chatService.getByRoomNum(roomNum);
    }

    @GetMapping("/chat")
    public Mono<String> getChatHtml() {
        return Mono.just("chat");
    }

    @GetMapping("/login")
    public Mono<String> getLoginHtml() {
        return Mono.just("login");
    }

    @GetMapping("/register")
    public Mono<String> getRegisterHtml() {
        return Mono.just("register");
    }
}