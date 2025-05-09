package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.rest.dto.ErrorResponse;
import ch.uzh.ifi.hase.soprafs24.rest.dto.TranslationResponseDTO;
import ch.uzh.ifi.hase.soprafs24.service.TranslationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
public class TranslateController {
    private final TranslationService translationService;

    public TranslateController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @GetMapping("/translate")
    public ResponseEntity<?> translate(@RequestParam String text, @RequestParam String target) {
        try {
            String result = translationService.translateText(text, target);
            return ResponseEntity.ok(new TranslationResponseDTO(result));
        }
        catch (ResponseStatusException ex) {
            ErrorResponse errorResponse = new ErrorResponse(ex.getReason());
            return ResponseEntity.status(ex.getStatus()).body(errorResponse);
        }
    }
}
