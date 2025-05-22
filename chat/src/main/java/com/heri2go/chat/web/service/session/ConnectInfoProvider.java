package com.heri2go.chat.web.service.session;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ConnectInfoProvider {
    public static final String SERVER_ID = UUID.randomUUID().toString().substring(0, 8); // 각 서버를 고유하게 식별하는 ID

    public String getRoomSessionsKey(String roomId) { // 특정 채팅방을 구독 중인 웹소켓 세션의 ID를 가져오는 키
        return "ServerId:" + SERVER_ID + "/RoomId:" + roomId;
    }

    public String getLastOnlineTimeKey(String username) { // 특정 유저의 마지막 온라인 시간을 가져오는 키
        return "LastOnline:" + username;
    }

    public String getRoomIdsKey(String sessionId) { // 특정 세션이 구독 중인 채팅방의 ID를 가져오는 키
        return "SessionId:" + sessionId;
    }

    public String getSessionIdKey(String userId) {
        return "UserId:" + userId;
    }
}
