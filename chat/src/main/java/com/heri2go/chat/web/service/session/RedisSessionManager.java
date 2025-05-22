package com.heri2go.chat.web.service.session;

import com.heri2go.chat.domain.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Redis 세션 저장소를 위한 새로운 클래스 생성
@RequiredArgsConstructor
@Service
public class RedisSessionManager {

    private final RedisDao redisDao;
    private final ConnectInfoProvider cip;


    public Mono<Void> saveRoomSession(String sessionId, String roomId, String userId) {
        String roomSessionsKey = cip.getRoomSessionsKey(roomId);
        String roomIdsKey = cip.getRoomIdsKey(sessionId);
        String sessionIdKey = cip.getSessionIdKey(userId);

        return redisDao.addToSet(roomSessionsKey, sessionId) // 채팅을 전파시켜야 할 세션들의 ID를 담는 키
                .then(redisDao.addToSet(roomIdsKey, roomId)) // 구독을 중지할 때 삭제할 Value를 매핑하기 위해 참조할 키
                .then(redisDao.setString(sessionIdKey, sessionId)) // 채팅방 생성 시 온라인 상태인 유저의 채팅 전파 채널을 열기 위해 참초할 키
                .then();
    }

    public Mono<Void> removeRoomSession(String sessionId, String userId) {
        String roomIdKey = cip.getRoomIdsKey(sessionId);
        return redisDao.getAllMembersOfSet(roomIdKey)
                .flatMap(roomId -> redisDao.removeFromSet(cip.getRoomSessionsKey(roomId), sessionId)
                            .then(redisDao.removeFromSet(roomIdKey, roomId))
                )
                .then(redisDao.delete(cip.getSessionIdKey(userId)))
                .then();
    }

    public Flux<String> getRoomSessionIds(String roomKey) { // 해당 채팅방에 접속 중인 웹소켓 세션 id 모두를 읽어오는 메서드
        return redisDao.getAllMembersOfSet(roomKey);
    }
}
