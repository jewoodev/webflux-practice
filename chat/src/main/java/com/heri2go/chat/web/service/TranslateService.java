package com.heri2go.chat.web.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;

@Slf4j
@Service
public class TranslateService {
    @Value("${google.credential-path}")
    private Resource jsonResource;

    public Mono<String> translate(String text, String srcLang, String targetLang) {
        return Mono.fromCallable(() -> {
                    try (InputStream serviceAccountStream = jsonResource.getInputStream()) {
                        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
                        Translate translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();

                        Translation translation = translate.translate(
                                text,
                                Translate.TranslateOption.sourceLanguage(srcLang),
                                Translate.TranslateOption.targetLanguage(targetLang)
                        );
                        return translation.getTranslatedText();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("번역 중 오류 발생: ", e);
                    return Mono.just(text);
                });
    }
}
