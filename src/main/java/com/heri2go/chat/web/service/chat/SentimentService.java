package com.heri2go.chat.web.service.chat;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SentimentService {
    private final RestTemplate googleLangRestTemplate;

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.api-key}")
    private String apiKey;

    private static final String GOOGLE_NLP_API_URL = "https://language.googleapis.com/v1/documents:analyzeSentiment";

    public Double analyzeSentiment(String text) {
        try {
            log.debug("Starting sentiment analysis for text: {}", text);
            Map<String, Object> requestBody = Map.of(
                "document", Map.of(
                    "type", "PLAIN_TEXT",
                    "content", text
                )
            );

            String url = GOOGLE_NLP_API_URL + "?key=" + apiKey;
            JsonNode response = googleLangRestTemplate.postForObject(url, requestBody, JsonNode.class);

            if (response != null) {
                JsonNode documentSentiment = response.get("documentSentiment");
                double score = documentSentiment.get("score").asDouble();
                log.debug("Sentiment analysis completed. Score: {}", score);
                return score;
            }
            return 0.0;
        } catch (Exception e) {
            log.error("감정 분석 API 호출 중 오류 발생: ", e);
            return 0.0;
        }
    }
}
