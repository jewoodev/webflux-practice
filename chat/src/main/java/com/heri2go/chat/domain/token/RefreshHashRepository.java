package com.heri2go.chat.domain.token;

import com.heri2go.chat.domain.RedisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Component
public class RefreshHashRepository {

    private final RedisDao redisDao;

    public Mono<RefreshHash> save(RefreshHash refreshHash) {
        String refreshHashKey = getRefreshHashKey(refreshHash.username());
        return redisDao.setString(refreshHashKey, refreshHash.refreshToken())
                .then(redisDao.expire(refreshHashKey, Duration.ofDays(14)))
                .then(Mono.just(refreshHash));
    }

    public Mono<String> findByUsername(String username) {
        String refreshHashKey = getRefreshHashKey(username);
        return redisDao.getString(refreshHashKey);
    }

    private static String getRefreshHashKey(String username) {
        return "RefreshHash:" + username;
    }
}
