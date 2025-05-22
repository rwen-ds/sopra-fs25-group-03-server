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

    protected TranslationServiceClient client;

    public String translateText(String text, String targetLanguage) {
        try {
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

