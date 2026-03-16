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

import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class TranslateService {
    @Value("${google.credential-path}")
    private Resource jsonResource;

    private final AtomicReference<Translate> translateService = new AtomicReference<>();

    private Translate getTranslateService() {
        Translate cached = translateService.get();
        if (cached != null) {
            return cached;
        }

        try (InputStream serviceAccountStream = jsonResource.getInputStream()) {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            Translate newTranslate = TranslateOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();
            translateService.set(newTranslate);
            return newTranslate;
        } catch (Exception e) {
            log.error("Google Translate 서비스 초기화 실패: ", e);
            throw new RuntimeException("Translation service initialization failed", e);
        }
    }

    public String translate(String text, String srcLang, String targetLang) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        try {
            Translate translate = getTranslateService();
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.sourceLanguage(srcLang),
                    Translate.TranslateOption.targetLanguage(targetLang)
            );
            return translation.getTranslatedText();
        } catch (Exception e) {
            log.error("번역 중 오류 발생: ", e);
            return text;
        }
    }
}
