package com.heri2go.chat.web.service.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

// Redis 세션 저장소를 위한 새로운 클래스 생성
@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ConnectInfoProvider cip;

    private static final Duration SESSION_TTL = Duration.ofHours(1);

    public Mono<Void> saveSession(String sessionId, String roomId, String username) {
        String sessionKey = cip.getSessionKey(sessionId);
        String roomKey = cip.getRoomKey(roomId);

        return redisTemplate.opsForHash().putAll(sessionKey, Map.of(
                        "roomName", roomId,
                        "username", username
                ))
                .then(redisTemplate.expire(sessionKey, SESSION_TTL))
                .then(redisTemplate.opsForSet().add(roomKey, sessionId))
                .then(redisTemplate.expire(roomKey, SESSION_TTL))
                .then();
    }

    public Flux<String> getRoomSessionsInServer(String roomId) {
        return redisTemplate.opsForHash().entries(cip.getRoomKey(roomId))
                .filter(entry -> entry.getValue().equals(roomId))
                .map(entry -> entry.getKey().toString());
    }

    public Mono<Void> removeSession(String sessionId) {
        return getSessionInfo(sessionId)
                .flatMap(sessionInfo ->
                        redisTemplate.opsForSet().remove(cip.getRoomKey(sessionInfo.get("roomName")), sessionId)
                            .then(redisTemplate.delete(cip.getSessionKey(sessionId)))
                )
                .then();
    }

    public Mono<Map<String, String>> getSessionInfo(String sessionId) {
        return redisTemplate.opsForHash().entries(cip.getSessionKey(sessionId))
                .collectMap(entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString());
    }

    public Flux<String> getRoomSessions(String roomId) {
        return redisTemplate.opsForSet().members(cip.getRoomKey(roomId));
    }
}
