package com.heri2go.chat.web.service.session;

import com.heri2go.chat.domain.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    public void saveRoomSession(String sessionId, String roomId, String userId) {
        String roomSessionsKey = cip.getRoomSessionsKey(roomId);
        String roomIdsKey = cip.getRoomIdsKey(sessionId);
        String sessionIdKey = cip.getSessionIdKey(userId);

        redisDao.addToSet(roomSessionsKey, sessionId);
        redisDao.addToSet(roomIdsKey, roomId);
        redisDao.setString(sessionIdKey, sessionId);
    }

    public void removeRoomSession(String sessionId, String userId) {
        String roomIdKey = cip.getRoomIdsKey(sessionId);
        Set<String> roomIds = redisDao.getAllMembersOfSet(roomIdKey);
        if (roomIds != null) {
            for (String roomId : roomIds) {
                redisDao.removeFromSet(cip.getRoomSessionsKey(roomId), sessionId);
                redisDao.removeFromSet(roomIdKey, roomId);
            }
        }
        redisDao.delete(cip.getSessionIdKey(userId));
    }

    public Set<String> getRoomSessionIds(String roomKey) {
        return redisDao.getAllMembersOfSet(roomKey);
    }
}
