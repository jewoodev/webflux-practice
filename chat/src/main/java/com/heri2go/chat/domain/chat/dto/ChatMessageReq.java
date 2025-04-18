package com.heri2go.chat.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
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
