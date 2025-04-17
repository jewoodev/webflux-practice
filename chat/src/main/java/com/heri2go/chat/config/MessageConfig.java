package com.heri2go.chat.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class MessageConfig {

    @Bean
    MessageListenerAdapter messageListenerAdapter(@Qualifier("messageDelegate") DefaultMessageDelegate listener) {
        return new MessageListenerAdapter(listener, "handleMessage");
    }

    @Bean
    ReactiveRedisMessageListenerContainer redisMessageListenerContainer(
            ReactiveRedisConnectionFactory mainRedisConFactory) {
        return new ReactiveRedisMessageListenerContainer(mainRedisConFactory);
    }

}
