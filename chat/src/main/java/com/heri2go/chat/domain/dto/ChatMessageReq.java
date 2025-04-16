package com.heri2go.chat.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatMessageReq {
    String sender;
    String msg;
    Long roomNum;
    String lang;

    public void setMsgAfterTranslate(String msg) {
        this.msg = msg;
    }
}
