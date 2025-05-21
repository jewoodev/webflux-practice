package com.heri2go.chat.web.service.session;

import com.heri2go.chat.domain.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

// Redis 세션 저장소를 위한 새로운 클래스 생성
@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;

    private static final Duration SESSION_TTL = Duration.ofHours(1);

    public Mono<Void> saveRoomSession(String sessionId, String roomId) {
        String roomSessionsKey = cip.getRoomSessionsKey(roomId);
        String roomIdsKey = cip.getRoomIdsKey(sessionId);

        return redisDao.addToSet(roomSessionsKey, sessionId) // 채팅을 전파시켜야 할 세션들의 ID를 담는 키
                .then(redisDao.expire(roomSessionsKey, SESSION_TTL))
                .then(redisDao.addToSet(roomIdsKey, roomId)) // 구독을 중지할 때 삭제할 Value를 매핑하기 위해 참조할 키
                .then(redisDao.expire(roomIdsKey, SESSION_TTL))
                .then();
    }

    public Mono<Void> removeRoomSession(String sessionId) {
        String roomIdKey = cip.getRoomIdsKey(sessionId);
        return redisDao.getAllMembersOfSet(roomIdKey)
                .flatMap(roomId -> redisDao.removeFromSet(cip.getRoomSessionsKey(roomId), sessionId)
                            .then(redisDao.removeFromSet(roomIdKey, roomId))
                )
                .then();
    }

    public Flux<String> getRoomSessionIds(String roomKey) { // 해당 채팅방에 접속 중인 웹소켓 세션 id 모두를 읽어오는 메서드
        return redisDao.getAllMembersOfSet(roomKey);
    }
}
