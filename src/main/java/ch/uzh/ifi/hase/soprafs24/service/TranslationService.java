package ch.uzh.ifi.hase.soprafs24.service;

import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TranslationService {

    private String projectId = "sopra-fs25-group-03-server";

    public String translateText(String text, String targetLanguage) {
        try (TranslationServiceClient client = TranslationServiceClient.create()) {
            TranslateTextRequest request = TranslateTextRequest.newBuilder()
                    .setParent("projects/" + projectId) // Replace with your project ID
                    .setTargetLanguageCode(targetLanguage)
                    .addContents(text)
                    .build();

            TranslateTextResponse response = client.translateText(request);
            return response.getTranslations(0).getTranslatedText();
        }
        catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Translation failed: " + e.getMessage());
        }
    }
}

