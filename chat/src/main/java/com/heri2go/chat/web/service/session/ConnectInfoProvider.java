package com.heri2go.chat.web.service.session;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConnectInfoProvider { // 세션 데이터는 모두 서버 기준으로 분산된다.
    // private final String SESSION_KEY_PREFIX = "session:";
    // private final String ROOM_KEY_PREFIX = "room:";
    public static final String SERVER_ID = UUID.randomUUID().toString().substring(0, 8);

    public String getSessionKey(String sessionId) {
        return SERVER_ID + ":" + sessionId;
    }

    public String getRoomKey(String roomId) {
        return SERVER_ID + ":" + roomId;
    }
}
