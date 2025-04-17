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
    String senderId; // 세션 식별을 위한 ID

    public void setMsgAfterTranslate(String msg) {
        this.msg = msg;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
