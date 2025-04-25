package com.heri2go.chat.web.controller.chat;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class ChatController {

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