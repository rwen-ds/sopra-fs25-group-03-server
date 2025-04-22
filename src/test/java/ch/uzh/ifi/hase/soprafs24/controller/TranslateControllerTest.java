package ch.uzh.ifi.hase.soprafs24.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.TranslationService;

@WebMvcTest(TranslateController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TranslateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @MockBean
    private AuthFilter authFilter;

    @Test
    public void testTranslate_success() throws Exception {
        String inputText = "Hello";
        String targetLang = "de";
        String translatedText = "Hallo";

        when(translationService.translateText(inputText, targetLang)).thenReturn(translatedText);

        mockMvc.perform(get("/translate")
                        .param("text", inputText)
                        .param("target", targetLang))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.translatedText").value("Hallo"));
    }


}