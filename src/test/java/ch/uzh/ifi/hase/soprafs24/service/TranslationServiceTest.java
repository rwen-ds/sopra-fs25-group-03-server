package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class TranslationServiceTest {

    @InjectMocks
    private TranslationService translationService;

    @Test
    public void translateText_withNullClient_throwsException() {
        String originalText = "Hello World";
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_emptyText_throwsException() {
        String originalText = "";
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_nullText_throwsException() {
        String originalText = null;
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_nullTargetLanguage_throwsException() {
        String originalText = "Hello World";
        String targetLanguage = null;

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_invalidLanguageCode_throwsException() {
        String originalText = "Hello World";
        String targetLanguage = "invalid_language_code_12345";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_longText_throwsException() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            longText.append("This is a very long text that might cause issues. ");
        }
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(longText.toString(), targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_specialCharacters_throwsException() {
        String originalText = "Hello! @#$%^&*()_+ 123 世界 🌍🚀";
        String targetLanguage = "ja";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_serviceNotAvailable_throwsInternalServerError() {
        String originalText = "Simple text";
        String targetLanguage = "fr";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("500 INTERNAL_SERVER_ERROR"));
        assertTrue(exception.getMessage().contains("Translation failed"));
    }
} 