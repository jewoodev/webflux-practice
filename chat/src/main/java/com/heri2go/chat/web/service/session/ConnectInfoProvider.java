package com.heri2go.chat.web.service.session;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConnectInfoProvider { // 세션 데이터는 모두 서버 기준으로 분산된다.
    public static final String SERVER_ID = UUID.randomUUID().toString().substring(0, 8);

    public String getSessionKey(String sessionId) {
        return SERVER_ID + ":session:" + sessionId;
    }

    public String getRoomKey(String roomId) {
        return SERVER_ID + ":room:" + roomId;
    }

    public String getLastOnlineTimeKey(String username) {
        return "last-online:" + username;
    }
}
