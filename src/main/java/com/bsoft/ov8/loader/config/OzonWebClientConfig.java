package com.bsoft.ov8.loader.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
//import org.springframework.http.client.ReactorClientHttpConnector;

import java.time.Duration;

@Slf4j
@Configuration
public class OzonWebClientConfig {

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    @Value("${api.ozon.api-key}") // New property for the API key
    private String ozonApiKey;

    @Bean
    public WebClient ozonWebClient(WebClient.Builder webClientBuilder) {
        log.info("Configuring Ozon WebClient with base URL: {}",  ozonBaseUrl);
        log.info("Configuring Ozon WebClient with API Key: {}" + (ozonApiKey != null && !ozonApiKey.isEmpty() ? ozonApiKey : "MISSING/EMPTY"));

        /*
        ObjectMapper objectMapper = new ObjectMapper()
                // IMPORTANT: This is the setting to allow unescaped control characters
                .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
                // You might also want to set this if the API sends unknown fields
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                }).build();
        */

        // --- Start of new connection pool configuration ---
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ozon-api-pool")
                .maxConnections(500) // Increase total connections, default is usually 50
                .maxIdleTime(Duration.ofSeconds(60)) // How long an unused connection stays in pool
                .maxLifeTime(Duration.ofSeconds(600)) // How long a connection can live in total
                .pendingAcquireTimeout(Duration.ofSeconds(10)) // How long to wait for a connection
                .pendingAcquireMaxCount(2000) // Increase the pending queue size, default is 1000
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30)) // Add a response timeout for slow APIs
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(20)) // Read timeout
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(20))); // Write timeout
        // --- End of new connection pool configuration ---

        return webClientBuilder
//                .clientConnector(new ReactorClientHttpConnector(httpClient))

                .baseUrl(ozonBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/hal+json") // Set the Accept header
                .defaultHeader("X-API-KEY", ozonApiKey) // Set the custom API key header
//                .exchangeStrategies(exchangeStrategies)
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
