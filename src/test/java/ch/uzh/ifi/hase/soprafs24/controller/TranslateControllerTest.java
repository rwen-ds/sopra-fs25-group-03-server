package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.Message;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.ContactDTO;
import ch.uzh.ifi.hase.soprafs24.security.AuthFilter;
import ch.uzh.ifi.hase.soprafs24.service.MessageService;
import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TranslateController.class)
@AutoConfigureMockMvc(addFilters = false)  // Disable the filter
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