package com.heri2go.chat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient googleLangClient() {
        return WebClient.builder()
                .baseUrl("https://language.googleapis.com")
                .build();
    }   
}