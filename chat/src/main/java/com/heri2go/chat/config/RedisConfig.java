package com.heri2go.chat.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Primary
    @Bean
    public ReactiveRedisConnectionFactory mainRedisConFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port
    ) {
        // Lettuce 클라이언트 설정
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(Duration.ofSeconds(30)) // 연결 시간 초과 설정
                                .keepAlive(SocketOptions.KeepAliveOptions.builder()
                                        .enable(true)
                                        .interval(Duration.ofSeconds(15))
                                        .count(3)
                                        .build())
                                .build())
                        .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                        .autoReconnect(true)
                        .build())
                .commandTimeout(Duration.ofSeconds(10)) // 명령 실행 제한 시간
                .shutdownTimeout(Duration.ofSeconds(3)) // 종료 시 최대 대기 시간
                .build();

        // Redis 서버 설정
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public ReactiveRedisOperations<String, String> mainRedisOperations(
            ReactiveRedisConnectionFactory mainRedisConFactory
    ) {
        RedisSerializer<String> serializer = StringRedisSerializer.UTF_8;
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(mainRedisConFactory, serializationContext);
    }
}
