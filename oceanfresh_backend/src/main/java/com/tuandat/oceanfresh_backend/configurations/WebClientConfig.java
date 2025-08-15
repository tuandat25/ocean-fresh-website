package com.tuandat.oceanfresh_backend.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Tạm thời disable OAuth2 để fix lỗi khởi động
@Configuration
public class WebClientConfig {

    private String introspectUri = "https://www.googleapis.com/oauth2/v3/tokeninfo";

    @Bean
    public WebClient userInfoClient() {
        return WebClient.builder().baseUrl(introspectUri).build();
    }
}