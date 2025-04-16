package com.heri2go.chat.web.controller;

import com.heri2go.chat.domain.dto.ChatMessageReq;
import com.heri2go.chat.domain.dto.ChatMessageResp;
import com.heri2go.chat.web.service.ChatService;
import com.heri2go.chat.web.service.TranslateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatController {

    private final ChatService chatService;
    private final TranslateService translateService;

    @MessageMapping("/chat.{chatRoomId}")
    @SendTo("/subscribe/chat.{chatRoomId}")
    public Mono<ChatMessageResp> sendMessage(ChatMessageReq req, @DestinationVariable Long chatRoomId) {

        String targetLang = req.getLang().equals("ko") ? "en" : "ko";

        return translateService.translate(req.getMsg(), req.getLang(), targetLang)
                .flatMap(translatedMsg -> {
                    req.setMsgAfterTranslate(translatedMsg);
                    return chatService.save(req);
                });
    }

    @GetMapping("/chat/{roomNum}")
    public Flux<ChatMessageResp> getChatHistory(@PathVariable Long roomNum) {
        return chatService.getByRoomNum(roomNum);
    }


    // SSE 구현 로직
//    @CrossOrigin
//    @GetMapping(value="/sender/{sender}/receiver/{receiver}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<Chat> getMsg(@PathVariable String sender, @PathVariable String receiver) {
//        return chatRepository.mfindBySender(sender, receiver)
//                .subscribeOn(Schedulers.boundedElastic());
//    }
//
//    @CrossOrigin
//    @GetMapping(value = "/chat/roomNum/{roomNum}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<Chat> getMsgByRoomNum(@PathVariable Integer roomNum) {
//        return chatRepository.mfindByRoomNum(roomNum)
//                .subscribeOn(Schedulers.boundedElastic())
//                .doOnNext(rn -> log.info("# doOnNext: {}", rn))
//                .doOnSubscribe(subscription -> log.info("# doOnSubscribe"));
//    }
//
//    @CrossOrigin
//    @PostMapping("/chat")
//    public Mono<Chat> sendMsg(@RequestBody Chat chat) {
//        chat.setCreatedAt(LocalDateTime.now());
//        return chatRepository.save(chat);
//    }
}
