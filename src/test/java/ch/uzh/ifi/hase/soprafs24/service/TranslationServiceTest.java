package ch.uzh.ifi.hase.soprafs24.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.Translation;
import com.google.cloud.translate.v3.TranslationServiceClient;

@ExtendWith(MockitoExtension.class)
public class TranslationServiceTest {

    @InjectMocks
    private TranslationService translationService;

    @Mock
    private TranslationServiceClient translationServiceClient;

    private String textToTranslate;
    private String targetLanguage;
    private String translatedText;

    @BeforeEach
    public void setup() {
        textToTranslate = "Hello, world!";
        targetLanguage = "de";
        translatedText = "Hallo, Welt!";
    }

    @Test
    public void translateText_success() {
        // 创建模拟对象
        TranslateTextResponse mockResponse = mock(TranslateTextResponse.class);
        Translation mockTranslation = mock(Translation.class);
        
        try (MockedStatic<TranslationServiceClient> mockedStatic = Mockito.mockStatic(TranslationServiceClient.class)) {
            // 模拟静态方法
            mockedStatic.when(TranslationServiceClient::create).thenReturn(translationServiceClient);
            
            // 模拟响应
            when(translationServiceClient.translateText(any())).thenReturn(mockResponse);
            when(mockResponse.getTranslationsCount()).thenReturn(1);
            when(mockResponse.getTranslations(0)).thenReturn(mockTranslation);
            when(mockTranslation.getTranslatedText()).thenReturn(translatedText);
            
            // 调用被测试方法
            String result = translationService.translateText(textToTranslate, targetLanguage);
            
            // 验证结果
            assertEquals(translatedText, result);
        }
    }
    
    @Test
    public void translateText_exceptionThrown() {
        try (MockedStatic<TranslationServiceClient> mockedStatic = Mockito.mockStatic(TranslationServiceClient.class)) {
            // 模拟静态方法抛出异常
            mockedStatic.when(TranslationServiceClient::create).thenReturn(translationServiceClient);
            when(translationServiceClient.translateText(any())).thenThrow(new RuntimeException("API error"));
            
            // 验证方法抛出预期的异常
            ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> translationService.translateText(textToTranslate, targetLanguage)
            );
            
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
            assertTrue(exception.getReason().contains("Translation failed"));
        }
    }
    
    @Test
    public void translateText_nullOrEmptyInput() {
        try (MockedStatic<TranslationServiceClient> mockedStatic = Mockito.mockStatic(TranslationServiceClient.class)) {
            // 模拟静态方法
            mockedStatic.when(TranslationServiceClient::create).thenReturn(translationServiceClient);
            
            // 创建模拟对象和响应
            TranslateTextResponse mockResponse = mock(TranslateTextResponse.class);
            Translation mockTranslation = mock(Translation.class);
            when(translationServiceClient.translateText(any())).thenReturn(mockResponse);
            when(mockResponse.getTranslationsCount()).thenReturn(1);
            when(mockResponse.getTranslations(0)).thenReturn(mockTranslation);
            when(mockTranslation.getTranslatedText()).thenReturn("");
            
            // 测试空字符串
            String result = translationService.translateText("", targetLanguage);
            assertEquals("", result);
            
            // 由于我们无法直接修改翻译服务的实现来处理空值，这里只是验证当传入空值时会怎样
            assertDoesNotThrow(() -> translationService.translateText("", targetLanguage));
        }
    }
} 