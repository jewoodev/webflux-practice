package com.heri2go.chat.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class RedisDao {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public Flux<? extends ReactiveSubscription.Message<String, String>> listenToPattern(String... patterns) {
        return redisTemplate.listenToPattern(patterns);
    }

    public Mono<Long> convertAndSend(String destination, String message) {
        return redisTemplate.convertAndSend(destination, message);
    }

    public Mono<Long> addToSet(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Flux<String> getAllMembersOfSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public Mono<Boolean> putAllToHash(String key, Map<String, String> map) {
        return redisTemplate.opsForHash().putAll(key, map);
    }

    public Flux<Map.Entry<Object, Object>> getEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Mono<Long> removeFromSet(String key, String... value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    public Mono<Long> delete(String... key) {
        return redisTemplate.delete(key);
    }

    public Mono<Boolean> expire(String key, Duration timeout) {
        return redisTemplate.expire(key, timeout);
    }

    public Mono<Boolean> setString(String key, String value) {
        return redisTemplate.opsForValue().set(key, value);
    }

    public Mono<LocalDateTime> getLastOnlineTime(String key) {
        return redisTemplate.opsForValue().get(key)
                .map(timeStr -> LocalDateTime.parse(timeStr))
                .defaultIfEmpty(LocalDateTime.MIN);
    }
}
