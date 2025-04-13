package ch.uzh.ifi.hase.soprafs24.rest.dto;


public class TranslationResponseDTO {
    private String translatedText;

    public TranslationResponseDTO() {
    }

    public TranslationResponseDTO(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}
