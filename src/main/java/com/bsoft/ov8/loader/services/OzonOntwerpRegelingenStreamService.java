package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.OntwerpRegelingDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.mappers.OntwerpRegelingMapper;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OzonOntwerpRegelingenStreamService {

    private final WebClient webClient;
    private final OntwerpRegelingMapper ontwerpRegelingMapper;
    private final OntwerpRegelingDTOSaver ontwerpRegelingDTOSaver;

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    public OzonOntwerpRegelingenStreamService(WebClient webClient,
                                              OntwerpRegelingDTOSaver ontwerpRegelingDTOSaver,
                                              OntwerpRegelingMapper ontwerpRegelingMapper

    ) {
        this.webClient = webClient;
        this.ontwerpRegelingDTOSaver = ontwerpRegelingDTOSaver;
        this.ontwerpRegelingMapper = ontwerpRegelingMapper;
    }

    /**
     * Fetches all ontwerpregelingen by traversing all pages provided by the API.
     *
     * @param beschikbaarOp The 'beschikbaarOp' datetime.
     * @param initialPage   Initial page number (can be null for default 1).
     * @param size          Page size (can be null for default 20).
     * @param sort          List of sorting parameters.
     * @param fields        Optional fields to include.
     * @return A Flux of all Regelingen across all pages.
     */
    public Flux<Ontwerpregeling> getAllOntwerpRegelingen(
                                                   OffsetDateTime beschikbaarOp,
                                                   Boolean _expand,
                                                   Integer initialPage,
                                                   Integer size,
                                                   List<OntwerpregelingenSort> sort,
                                                   String fields
    ) {
        String initialUri = buildInitialUri(
                beschikbaarOp,
                _expand,
                initialPage,
                size,
                sort,
                fields
        );

        return fetchPage(initialUri) // Use the helper method here
                .expand(response -> { // 'response' is now of type Ontwerpregelingen
                    if (response.getLinks() != null && response.getLinks().getNext() != null) {
                        String nextLink = response.getLinks().getNext().getHref().toString(); // Get URI and convert to String
                        return fetchPage(nextLink); // Fetch the next page
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(regelingenResponse -> { // 'regelingenResponse' is now of type Regelingen
                    if (regelingenResponse.getEmbedded() != null && regelingenResponse.getEmbedded().getOntwerpregelingen() != null) {
                        // Assuming RegelingenEmbedded has a getRegelingen() method that returns List<Regeling>
                        return Flux.fromIterable(regelingenResponse.getEmbedded().getOntwerpregelingen());
                    }
                    return Flux.empty();
                });
    }

    /**
     * Helper method to fetch a single page of ontwerpregelingen.
     */
    private Mono<Ontwerpregelingen> fetchPage(String uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Ontwerpregelingen.class) // Correct type here
                .doOnError(e -> System.err.println("Error fetching page " + uri + ": " + e.getMessage()));
    }

    /**
     * Helper method to build the initial URI with query parameters.
     */
    private String buildInitialUri(
            OffsetDateTime beschikbaarOp,
            Boolean _expand,
            Integer page,
            Integer size,
            List<OntwerpregelingenSort> sort,
            String fields
    ) {
        // Define the base URL for the external service
        String baseUrl = ozonBaseUrl;

        // Use UriComponentsBuilder directly to construct the URI
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/ontwerpregelingen")
                .queryParam("beschikbaarOp", beschikbaarOp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // Add optional parameters using queryParamIfPresent for robustness
        Optional.ofNullable(page).ifPresent(val -> uriBuilder.queryParam("page", val));
        Optional.ofNullable(size).ifPresent(val -> uriBuilder.queryParam("size", val));
        Optional.ofNullable(_expand).ifPresent(val -> uriBuilder.queryParam("_expand", val));

        // Handle the 'sort' parameter, joining multiple if needed
        if (sort != null && !sort.isEmpty()) {
            String sortString = sort.stream()
                    .map(OntwerpregelingenSort::name)
                    .collect(Collectors.joining(","));
            uriBuilder.queryParam("_sort", sortString);
        }

        Optional.ofNullable(fields).ifPresent(val -> uriBuilder.queryParam("fields", val));

        // Build and return the URI as a String
        return uriBuilder.build().toUriString();
    }

    public void procesAll(
            OffsetDateTime beschikbaarOp,
            Boolean _expand,
            Integer initialPage,
            Integer size,
            List<OntwerpregelingenSort> sort,
            String fields
    ) {
        final long start = System.currentTimeMillis();

        getAllOntwerpRegelingen(beschikbaarOp, _expand, initialPage, size, sort, fields)
                .doOnNext(ontwerpregeling -> {
                    log.debug("Processing ontwerpregeling {}", ontwerpregeling.getIdentificatie());
                    OntwerpRegelingDTO ontwerpRegelingDTO = ontwerpRegelingMapper.toOntwerpRegelingDTO(ontwerpregeling);
                    ontwerpRegelingDTOSaver.saveOntwerpregeling(ontwerpRegelingDTO, ontwerpregeling);
                })
                .doOnError(e -> log.error("Error during processing individual Ontwerpregeling: {}", e.getMessage()))
                .doOnComplete(() -> {
                    log.debug("All ontwerpregelingen processing complete");
                    log.info("Duration: " + (System.currentTimeMillis() - start));
                })
                .subscribe();
    }

}