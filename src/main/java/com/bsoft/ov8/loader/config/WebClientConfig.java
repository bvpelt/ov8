package com.bsoft.ov8.loader.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${api.ozon.presenteren.base-url}")
    private String ozonPresenterenBaseUrl;

    @Value("${api.ozon.download.base-url}")
    private String ozonDownloadBaseUrl;

    @Value("${api.ozon.api-key}")
    private String ozonApiKey;

    @Value("${webclient.max-in-memory-size:200MB}")
    private DataSize maxInMemorySize;

    /**
     * Common WebClient builder with shared configuration (buffer size, API key, logging)
     */
    private WebClient.Builder createBaseWebClientBuilder() {
        log.debug("Using http maxmemorysize: {}", maxInMemorySize.toString());
        return WebClient.builder()
                // Maximum buffer size for downloads
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> {
                            configurer.defaultCodecs().maxInMemorySize((int) maxInMemorySize.toBytes());
                        })
                        .build())
                .defaultHeader("x-api-key", ozonApiKey) // Common API key header
                // Add the request/response logging filter
                .filter((request, next) -> {
                    log.debug("---- Outgoing WebClient Request (Filter) ----");
                    log.debug("URI: " + request.url());
                    log.debug("Method: " + request.method());

                    // Log query parameters
                    if (request.url().getQuery() != null && !request.url().getQuery().isEmpty()) {
                        log.debug("Query Parameters");
                        String[] params = request.url().getQuery().split("&");
                        for (String param : params) {
                            String[] keyValue = param.split("=", 2);
                            if (keyValue.length == 2) {
                                String key = java.net.URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                                String value = java.net.URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                                log.debug("  {} = {}", key, value);
                            } else {
                                log.debug("  {} (no value)", keyValue[0]);
                            }
                        }
                    } else {
                        log.debug("No query parameters");
                    }

                    // Log headers
                    log.debug("Headers:");
                    request.headers().forEach((name, values) -> {
                        log.debug("  {}: {}", name, String.join(", ", values));
                    });

                    // Log body if present (be careful with large payloads)
                    if (request.body() != null) {
                        log.debug("Body: present (type: {})", request.body().getClass().getSimpleName());
                    }

                    log.debug("----------------------------------------------");
                    return next.exchange(request);
                });
    }

    /**
     * WebClient for Ozon Regelingen service (HAL+JSON)
     */
    @Bean("ozonRegelingenWebClient")
    public WebClient ozonRegelingenWebClient() {
        log.info("Configuring Ozon Regelingen WebClient with base URL: {}", ozonPresenterenBaseUrl);

        return createBaseWebClientBuilder()
                .baseUrl(ozonPresenterenBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/hal+json")
                .build();
    }

    /**
     * WebClient for Ozon Geo Download service (JSON)
     */
    @Bean("ozonGeoDownloadWebClient")
    public WebClient ozonGeoDownloadWebClient() {
        log.info("Configuring Ozon Geo Download WebClient with base URL: {}", ozonDownloadBaseUrl);

        return createBaseWebClientBuilder()
                .baseUrl(ozonDownloadBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader("x-api-key", ozonApiKey) // Some APIs might need lowercase
                .build();
    }

    /**
     * Generic WebClient without base URL or specific Accept header
     * Useful for services that need to call different endpoints
     */
    @Bean("genericWebClient")
    @Primary
    public WebClient genericWebClient() {
        log.info("Configuring Generic WebClient");

        return createBaseWebClientBuilder()
                .build();
    }
}
