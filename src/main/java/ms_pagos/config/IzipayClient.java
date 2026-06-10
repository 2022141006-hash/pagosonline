package ms_pagos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.RequiredArgsConstructor;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IzipayClient {

    private final WebClient webClient;

    @Value("${izipay.merchant-code}")
    private String merchantCode;

    @Value("${izipay.public-key}")
    private String publicKey;

    @Value("${izipay.username}")
    private String username;

    @Value("${izipay.password}")
    private String password;

    public String generarToken() {
        String credenciales = username + ":" + password;
        String basicAuth = Base64.getEncoder()
                .encodeToString(credenciales.getBytes());

        Map response = webClient.post()
                .uri("/security/v1/Token")
                .header("Authorization", "Basic " + basicAuth)
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        return String.valueOf(response.get("token"));
    }

    public Map<String, Object> procesarPago(Map<String, Object> request) {
        String token = generarToken();
        return webClient.post()
                .uri("/api/v1/pay")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}