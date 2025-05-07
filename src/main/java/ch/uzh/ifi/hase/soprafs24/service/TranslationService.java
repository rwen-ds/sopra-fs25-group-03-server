package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;

@Service
public class TranslationService {

    private String projectId = "sopra-fs25-group-03-server";
    
    // 添加一个客户端字段，允许在测试中注入mock
    protected TranslationServiceClient client;

    public String translateText(String text, String targetLanguage) {
        try {
            // 如果client为null，则创建一个新的客户端
            TranslationServiceClient translationClient = client != null ? client : TranslationServiceClient.create();
            
            try (TranslationServiceClient actualClient = translationClient) {
                TranslateTextRequest request = TranslateTextRequest.newBuilder()
                        .setParent("projects/" + projectId)
                        .setTargetLanguageCode(targetLanguage)
                        .addContents(text)
                        .build();
    
                TranslateTextResponse response = actualClient.translateText(request);
                return response.getTranslations(0).getTranslatedText();
            }
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Translation failed: " + e.getMessage());
        }
    }
}

