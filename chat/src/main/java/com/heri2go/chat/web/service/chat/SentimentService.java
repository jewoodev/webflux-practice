package com.heri2go.chat.web.service.chat;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RequiredArgsConstructor
@Service
public class SentimentService {
    private final WebClient googleLangClient;
    
    @Value("${google.cloud.project-id}")
    private String projectId;
    
    @Value("${google.cloud.api-key}")
    private String apiKey;

    private static final String GOOGLE_NLP_API_URL = "/v1/documents:analyzeSentiment";

    public Mono<Double> analyzeSentiment(String text) {
        return Mono.defer(() -> {
            log.debug("Starting sentiment analysis for text: {}", text);
            Map<String, Object> requestBody = Map.of(
                "document", Map.of(
                    "type", "PLAIN_TEXT",
                    "content", text
                )
            );
            
            return googleLangClient.post()
                    .uri(uriBuilder -> uriBuilder
                        .path(GOOGLE_NLP_API_URL)
                        .queryParam("key", apiKey)
                        .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5)) // 타임아웃 설정
                    .subscribeOn(Schedulers.boundedElastic()) // I/O 작업을 위한 전용 스케줄러
                    .map(response -> {
                        JsonNode documentSentiment = response.get("documentSentiment");
                        double score = documentSentiment.get("score").asDouble();
                        double magnitude = documentSentiment.get("magnitude").asDouble();
                        
                        log.debug("Sentiment analysis completed. Score: {}", score);
                        return score;
                    });
        })
        .onErrorResume(e -> {
            log.error("감정 분석 API 호출 중 오류 발생: ", e);
            return Mono.just(0.0); // 오류 발생 시 중립 점수 반환
        });
    }
}
