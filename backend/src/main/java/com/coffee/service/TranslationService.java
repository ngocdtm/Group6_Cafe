//package com.coffee.service;
//
//import com.google.cloud.translate.Detection;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.Translation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class TranslationService {
//
//    private final Translate translate;
//
//    @Autowired
//    public TranslationService(Translate translate) {
//        this.translate = translate;
//    }
//
//    public String translateText(String text, String targetLanguage) {
//        if (text == null || text.isEmpty()) {
//            return "";
//        }
//
//        // Detect source language
//        Detection detection = translate.detect(text);
//        String sourceLanguage = detection.getLanguage();
//
//        // Don't translate if source and target languages are the same
//        if (sourceLanguage.equals(targetLanguage)) {
//            return text;
//        }
//
//        // Perform translation
//        Translation translation = translate.translate(
//                text,
//                Translate.TranslateOption.sourceLanguage(sourceLanguage),
//                Translate.TranslateOption.targetLanguage(targetLanguage)
//        );
//
//        return translation.getTranslatedText();
//    }
//
//    public String translateToEnglish(String text) {
//        return translateText(text, "en");
//    }
//
//    public String translateToVietnamese(String text) {
//        return translateText(text, "vi");
//    }
//}