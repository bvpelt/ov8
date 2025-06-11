package com.bsoft.ov8.loader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;


@Slf4j
@Configuration
public class OzonWebClientConfig {

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    @Value("${api.ozon.api-key}") // New property for the API key
    private String ozonApiKey;

    @Bean
    public WebClient ozonWebClient(WebClient.Builder webClientBuilder) {
        log.info("Configuring Ozon WebClient with base URL: {}", ozonBaseUrl);
        log.info("Configuring Ozon WebClient with API Key: {}" + (ozonApiKey != null && !ozonApiKey.isEmpty() ? ozonApiKey : "MISSING/EMPTY"));

        return webClientBuilder
                .baseUrl(ozonBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/hal+json") // Set the Accept header
                .defaultHeader("X-API-KEY", ozonApiKey) // Set the custom API key header
                // Add the request/response logging filter here
                .filter((request, next) -> {
                    System.out.println("---- Outgoing WebClient Request (Filter) ----");
                    System.out.println("URI: " + request.url());
                    System.out.println("Method: " + request.method());
                    request.headers().forEach((name, values) -> System.out.println(name + ": " + values));
                    System.out.println("----------------------------------------------");
                    return next.exchange(request);
                })
                .build();
    }
}
