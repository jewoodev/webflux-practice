package com.heri2go.chat.web.controller.chat.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class ChatCreateRequest {
    private String sender;
    private String msg;
    private Long roomNum;
    private String lang;
    private MessageType type;


    public void setMsgAfterTranslate(String msg) {
        this.msg = msg;
    }

    public enum MessageType {
        ENTER, TALK, LEAVE
    }
}
