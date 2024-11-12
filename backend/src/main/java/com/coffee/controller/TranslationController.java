//package com.coffee.controller;
//
//import com.coffee.service.TranslationService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/v1/translation")
//public class TranslationController {
//
//    @Autowired
//    private TranslationService translationService;
//
//    @Operation(
//            summary = "Translate Text",
//            description = "Endpoint to translate text between languages"
//    )
//    @PostMapping("/translate")
//    public ResponseEntity<String> translate(
//            @RequestParam String text,
//            @RequestParam String targetLanguage) {
//        try {
//            String translatedText = translationService.translateText(text, targetLanguage);
//            return ResponseEntity.ok(translatedText);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Translation failed: " + e.getMessage());
//        }
//    }
//
//    @Operation(
//            summary = "Translate to English",
//            description = "Endpoint to translate text to English"
//    )
//    @PostMapping("/toEnglish")
//    public ResponseEntity<String> translateToEnglish(@RequestParam String text) {
//        try {
//            String translatedText = translationService.translateToEnglish(text);
//            return ResponseEntity.ok(translatedText);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Translation failed: " + e.getMessage());
//        }
//    }
//
//    @Operation(
//            summary = "Translate to Vietnamese",
//            description = "Endpoint to translate text to Vietnamese"
//    )
//    @PostMapping("/toVietnamese")
//    public ResponseEntity<String> translateToVietnamese(@RequestParam String text) {
//        try {
//            String translatedText = translationService.translateToVietnamese(text);
//            return ResponseEntity.ok(translatedText);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Translation failed: " + e.getMessage());
//        }
//    }
//}