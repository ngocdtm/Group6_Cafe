//package com.coffee.config;
//
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class TranslationConfig {
//
//    @Value("${google.cloud.translation.api-key}")
//    private String apiKey;
//
//    @Bean
//    public Translate translate() {
//        return TranslateOptions.newBuilder()
//                .setApiKey(apiKey)
//                .build()
//                .getService();
//    }
//}