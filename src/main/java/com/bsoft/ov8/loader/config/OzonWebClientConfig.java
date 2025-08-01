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

    @Value("${api.ozon.presenteren.base-url}")
    private String ozonBaseUrl;

    @Value("${api.ozon.api-key}") // New property for the API key
    private String ozonApiKey;

    @Bean
    public WebClient ozonWebClient() {
        log.info("Configuring Ozon WebClient with base URL: {}", ozonBaseUrl);
        log.info("Configuring Ozon WebClient with API Key: {}", ozonApiKey);

        return WebClient.builder()
                .baseUrl(ozonBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/hal+json") // Set the Accept header
                .defaultHeader("X-API-KEY", ozonApiKey) // Set the custom API key header
                // Add the request/response logging filter here
                .filter((request, next) -> {
                    log.debug("---- Outgoing WebClient Request (Filter) ----");
                    log.debug("URI: " + request.url());
                    log.debug("Method: " + request.method());
                    request.headers().forEach((name, values) -> log.debug(name + ": " + values));
                    log.debug("----------------------------------------------");
                    return next.exchange(request);
                })
                .build();
    }
}
