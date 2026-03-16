package com.heri2go.chat.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisDao {

    private final StringRedisTemplate redisTemplate;

    public void convertAndSend(String destination, String message) {
        redisTemplate.convertAndSend(destination, message);
    }

    public Long addToSet(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Set<String> getAllMembersOfSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public void putToHash(String key, String mapKey, String mapValue) {
        redisTemplate.opsForHash().put(key, mapKey, mapValue);
    }

    public void putAllToHash(String key, Map<?, ?> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public Map<Object, Object> getEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Long removeFromSet(String key, Object... value) {
        return redisTemplate.opsForSet().remove(key, value);
    }

    public void delete(String... key) {
        for (String k : key) {
            redisTemplate.delete(k);
        }
    }

    public Boolean expire(String key, Duration timeout) {
        return redisTemplate.expire(key, timeout);
    }

    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public LocalDateTime getLastOnlineTime(String key) {
        String timeStr = redisTemplate.opsForValue().get(key);
        if (timeStr == null) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.parse(timeStr);
    }

    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
