package com.heri2go.chat.web.service.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static com.heri2go.chat.web.service.session.SessionKey.*;

import java.time.Duration;
import java.util.Map;

// Redis 세션 저장소를 위한 새로운 클래스 생성
@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    private static final Duration SESSION_TTL = Duration.ofHours(1);

    public Mono<Void> saveSession(String sessionId, String roomId, String username) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        String roomKey = ROOM_KEY_PREFIX + roomId;

        return redisTemplate.opsForHash().putAll(sessionKey, Map.of(
                        "roomId", roomId,
                        "username", username
                ))
                .then(redisTemplate.expire(sessionKey, SESSION_TTL))
                .then(redisTemplate.opsForSet().add(roomKey, sessionId))
                .then(redisTemplate.expire(roomKey, SESSION_TTL))
                .then();
    }

    public Mono<Void> removeSession(String sessionId) {
        return getSessionInfo(sessionId)
                .flatMap(sessionInfo -> {
                    String roomKey = ROOM_KEY_PREFIX + sessionInfo.get("roomId");
                    return redisTemplate.opsForSet().remove(roomKey, sessionId)
                            .then(redisTemplate.delete(SESSION_KEY_PREFIX + sessionId));
                })
                .then();
    }

    public Mono<Map<String, String>> getSessionInfo(String sessionId) {
        return redisTemplate.opsForHash().entries(SESSION_KEY_PREFIX + sessionId)
                .collectMap(entry -> entry.getKey().toString(),
                        entry -> entry.getValue().toString());
    }

    public Flux<String> getRoomSessions(String roomId) {
        return redisTemplate.opsForSet().members(ROOM_KEY_PREFIX + roomId);
    }
}
