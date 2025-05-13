package com.ByteShield.HerbaWash.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient keyclakWebClient(){
        return WebClient
                .builder()
                .baseUrl("http://localhost:8080")
                .build();
    }
}
