package com.heri2go.chat.web.controller.chat.request;

import lombok.Builder;

@Builder
public record ChatCreateRequest(
    String sender,
    String msg,
    Long roomNum,
    String lang,
    MessageType type
) {
    public static ChatCreateRequest withTranslatedMsg(ChatCreateRequest req, String translatedMessage) {
        return ChatCreateRequest.builder()
                .sender(req.sender())
                .msg(translatedMessage)
                .roomNum(req.roomNum())
                .lang(req.lang())
                .type(req.type())
                .build();
    }

    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}
