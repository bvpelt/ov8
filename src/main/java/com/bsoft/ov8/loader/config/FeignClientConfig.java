package com.bsoft.ov8.loader.config;


import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FeignClientConfig {

    // Inject the API key from application.yml
    @Value("${api.ozon.key}")
    private String ozonApiKey;

    /**
     * Creates a RequestInterceptor bean that adds the API key to requests.
     * The header name ("X-API-Key" or "Authorization") depends on the API specification.
     * Often, it's "X-API-Key" or "Authorization" with a "Bearer " prefix if it's a token.
     */
    @Bean
    public RequestInterceptor apiKeyRequestInterceptor() {
        return requestTemplate -> {
            // Check your API spec for the correct header name. Common examples:
            // "X-API-Key"
            // "Authorization" (often with "Bearer " prefix for tokens, but sometimes raw API keys too)

            // Example 1: If API expects a header like "X-API-Key: your_super_secret_key"
            requestTemplate.header("X-API-Key", ozonApiKey);

            // Example 2: If API expects "Authorization: ApiKey your_super_secret_key"
            // requestTemplate.header("Authorization", "ApiKey " + ozonApiKey);

            // Example 3: If API expects "Authorization: Bearer your_super_secret_key" (less common for raw API keys, more for OAuth2)
            // requestTemplate.header("Authorization", "Bearer " + ozonApiKey);

            log.info("Adding API Key header to Feign request for URL: " + requestTemplate.url());
        };
    }
}
