package com.heri2go.chat.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Component("messageDelegate")
public class DefaultMessageDelegate implements MessageListener {

    private final ReactiveRedisOperations<String, String> mainRedisOperations;
    private final ObjectMapper objectMapper;

    private static final String REDIS_CHANNEL_PREFIX = "chat:room:";

    // 각 채팅방별 메시지 Sink
    private final ConcurrentHashMap<String, Sinks.Many<String>> roomSinks = new ConcurrentHashMap<>();
    
    // 전체 메시지 브로드캐스팅을 위한 Sink
    private final Sinks.Many<String> globalSink = Sinks.many().multicast().onBackpressureBuffer();
    
    // Redis 구독 등록 상태
    private final ConcurrentHashMap<String, Boolean> subscribedRooms = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("DefaultMessageDelegate 초기화");
        
        // Redis 키 패턴 감시를 통해 동적으로 채팅방 구독
        mainRedisOperations.keys(REDIS_CHANNEL_PREFIX + "*")
                .doOnNext(roomId -> {
                    log.info("초기 채팅방 발견: {}", roomId);
                    subscribeToRoom(roomId);
                })
                .subscribe();
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("DefaultMessageDelegate 정리");
        // 모든 sink 종료
        roomSinks.forEach((roomId, sink) -> sink.tryEmitComplete());
        globalSink.tryEmitComplete();
    }
    
    /**
     * 채팅방에 구독
     */
    public void subscribeToRoom(String roomId) {
        if (subscribedRooms.putIfAbsent(roomId, true) == null) {
            log.info("채팅방 구독 시작: {}", roomId);
            
            // 해당 채팅방의 Sink 생성
            Sinks.Many<String> roomSink = Sinks.many().multicast().onBackpressureBuffer();
            roomSinks.put(roomId, roomSink);
            
            // Redis 채널 구독
            mainRedisOperations.listenToChannel(roomId)
                    .doOnSubscribe(s -> log.info("Redis 채널 구독 성공: {}", roomId))
                    .doOnError(e -> {
                        log.error("Redis 채널 구독 오류: {}", e.getMessage(), e);
                        subscribedRooms.remove(roomId);
                    })
                    .map(ReactiveSubscription.Message::getMessage)
                    .doOnNext(message -> {
                        log.debug("Redis 메시지 수신: channel={}, message={}", roomId, message);
                        
                        // 메시지를 해당 채팅방의 Sink로 전송
                        roomSink.tryEmitNext(message);
                        
                        // 전역 메시지 Sink로도 전송
                        globalSink.tryEmitNext(message);
                    })
                    .subscribe();
        }
    }
    
    /**
     * 모든 채팅방의 메시지 스트림 반환
     */
    public Flux<String> getMessageStream() {
        return globalSink.asFlux();
    }
    
    /**
     * 특정 채팅방의 메시지 스트림 반환
     */
    public Flux<String> getMessageStream(String roomId) {
        String redisRoomKey = REDIS_CHANNEL_PREFIX + roomId;
        
        // 아직 구독하지 않은 채팅방인 경우 구독 시작
        if (!subscribedRooms.containsKey(redisRoomKey)) {
            subscribeToRoom(redisRoomKey);
        }
        
        return roomSinks.computeIfAbsent(
                redisRoomKey,
                k -> Sinks.many().multicast().onBackpressureBuffer()
        ).asFlux();
    }
    
    /**
     * 메시지를 Redis에 발행
     */
    public Mono<Long> publishMessage(String roomId, Object message) {
        String redisRoomKey = REDIS_CHANNEL_PREFIX + roomId;
        
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            log.debug("Redis 메시지 발행: 채널={}, 메시지={}", redisRoomKey, messageJson);
            
            return mainRedisOperations.convertAndSend(redisRoomKey, messageJson)
                    .doOnSuccess(count -> log.debug("Redis 메시지 발행 성공: 채널={}, 구독자 수={}", redisRoomKey, count))
                    .doOnError(e -> log.error("Redis 메시지 발행 실패: {}", e.getMessage(), e));
        } catch (JsonProcessingException e) {
            log.error("메시지 직렬화 실패: {}", e.getMessage(), e);
            return Mono.error(e);
        }
    }
    
    /**
     * MessageListenerAdapter에서 사용하는 메시지 처리 메서드
     * RedisMessageListenerContainer와 함께 사용
     */
    public void handleMessage(String message, String channel) {
        log.debug("Redis 메시지 수신(동기적): channel={}, message={}", channel, message);
        
        // 채널에 해당하는 Sink가 있는지 확인
        Sinks.Many<String> roomSink = roomSinks.get(channel);
        if (roomSink != null) {
            roomSink.tryEmitNext(message);
        }
        
        // 전역 메시지 Sink로도 전송
        globalSink.tryEmitNext(message);
    }
    
    /**
     * MessageListener 인터페이스 구현 메서드
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String payload = new String(message.getBody());
        
        log.debug("Redis 메시지 수신(리스너): channel={}, message={}", channel, payload);
        
        // handleMessage 메서드로 위임
        handleMessage(payload, channel);
    }
}