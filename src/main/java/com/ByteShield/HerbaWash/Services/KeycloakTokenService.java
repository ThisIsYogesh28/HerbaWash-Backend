package com.ByteShield.HerbaWash.Services;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakTokenService {
    private final WebClient keycloakWebClient;
    @Value("${keycloak.token-uri}")
    private String tokenUri;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.admin.username}")
    private String adminUsername;
    @Value("${keycloak.admin.password}")
    private String adminPassword;
    @Value("${keycloak.client.id}")
    private String clientID;
    @Value("${keycloak.client.secret}")
    private String clientSecret;

    public String fetchAccessToken(){
        String url=tokenUri.replace("{realm}",realm);
        Mono<Map> tokenResponse=keycloakWebClient.post()
                .uri("/realms/"+realm+"/protocol/openid-connect/token")
                .header("Content-Type","application/x-www-form-urlencoded")
                .bodyValue("client_id="+clientID+"&client_secret="+clientSecret+"&username="+adminUsername+"&password="+adminPassword+"&grant_type=password")
                .retrieve()
                .bodyToMono(Map.class);
        Map response=tokenResponse.block();
        if (response!=null && response.containsKey("access_token"))
            return response.get("access_token").toString();
        else throw new RuntimeException("Failed to fetch exception from the keycloak");
    }

}
