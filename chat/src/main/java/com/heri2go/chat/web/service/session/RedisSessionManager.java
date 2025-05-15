package com.heri2go.chat.web.service.session;

import com.heri2go.chat.domain.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

// Redis 세션 저장소를 위한 새로운 클래스 생성
@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    private static final Duration SESSION_TTL = Duration.ofHours(1);

    public Mono<Void> saveSession(String sessionId, String roomId, String username) {
        String sessionKey = cip.getSessionKey(sessionId);
        String roomKey = cip.getRoomKey(roomId);

        return redisDao.putAllToHash(sessionKey, Map.of(
                        "roomId", roomId,
                        "username", username
                ))
                .then(redisDao.expire(sessionKey, SESSION_TTL))
                .then(redisDao.addToSet(roomKey, sessionId))
                .then(redisDao.expire(roomKey, SESSION_TTL))
                .then();
    }

    public Flux<String> getRoomSessionsInServer(String roomId) {
        return redisDao.getEntries(cip.getRoomKey(roomId))
                .filter(entry -> entry.getValue().equals(roomId))
                .map(entry -> entry.getKey().toString());
    }

    public Mono<Void> removeSession(String sessionId) {
        return getSessionInfo(sessionId)
                .flatMap(sessionInfo ->
                        redisDao.removeFromSet(cip.getRoomKey(sessionInfo.get("roomId")), sessionId)
                            .then(redisDao.delete(cip.getSessionKey(sessionId)))
                )
                .then();
    }

    public Mono<Map<String, String>> getSessionInfo(String sessionId) {
        return redisDao.getEntries(cip.getSessionKey(sessionId))
                .collectMap(entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString());
    }

    public Flux<String> getRoomSessionIds(String roomKey) { // 해당 채팅방에 접속 중인 웹소켓 세션 id 모두를 읽어오는 메서드
        return redisDao.getAllMembersOfSet(roomKey);
    }
}
