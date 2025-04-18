package com.heri2go.chat.web.service.chat;

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
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TranslateService {
    @Value("${google.credential-path}")
    private Resource jsonResource;

    private final AtomicReference<Translate> translateService = new AtomicReference<>();
    private final Mono<Translate> initializerMono;

    public TranslateService() {
        // 지연 초기화를 위한 Mono 정의
        this.initializerMono = Mono.defer(() -> {
            Translate cachedService = translateService.get();
            if (cachedService != null) {
                return Mono.just(cachedService);
            }

            return Mono.fromCallable(() -> {
                        try (InputStream serviceAccountStream = jsonResource.getInputStream()) {
                            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
                            Translate newTranslate = TranslateOptions.newBuilder()
                                    .setCredentials(credentials)
                                    .build()
                                    .getService();
                            translateService.set(newTranslate);
                            return newTranslate;
                        }
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .cache(); // 결과 캐싱
        });
    }


    public Mono<String> translate(String text, String srcLang, String targetLang) {
        if (text == null || text.isEmpty()) {
            return Mono.just("");
        }

        return initializerMono
                .flatMap(translate -> Mono.fromCallable(() -> {
                    Translation translation = translate.translate(
                            text,
                            Translate.TranslateOption.sourceLanguage(srcLang),
                            Translate.TranslateOption.targetLanguage(targetLang)
                    );
                    return translation.getTranslatedText();
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    if (e instanceof TimeoutException || (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
                        log.error("번역 요청 타임아웃: {}", text);
                    } else {
                        log.error("번역 중 오류 발생: ", e);
                    }
                    return Mono.just(text);
                });
    }

}
