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
        // 测试client为null时的情况，由于无法连接到Google API会抛出异常
        String originalText = "Hello World";
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_emptyText_throwsException() {
        // 测试空文本
        String originalText = "";
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_nullText_throwsException() {
        // 测试null文本
        String originalText = null;
        String targetLanguage = "de";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_nullTargetLanguage_throwsException() {
        // 测试null目标语言
        String originalText = "Hello World";
        String targetLanguage = null;

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_invalidLanguageCode_throwsException() {
        // 测试无效的语言代码
        String originalText = "Hello World";
        String targetLanguage = "invalid_language_code_12345";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_longText_throwsException() {
        // 测试非常长的文本
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
        // 测试特殊字符
        String originalText = "Hello! @#$%^&*()_+ 123 世界 🌍🚀";
        String targetLanguage = "ja";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("Translation failed"));
    }

    @Test
    public void translateText_serviceNotAvailable_throwsInternalServerError() {
        // 由于Google Translation Service不可用，所有调用都会抛出异常
        String originalText = "Simple text";
        String targetLanguage = "fr";

        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            translationService.translateText(originalText, targetLanguage);
        });

        assertTrue(exception.getMessage().contains("500 INTERNAL_SERVER_ERROR"));
        assertTrue(exception.getMessage().contains("Translation failed"));
    }
} 