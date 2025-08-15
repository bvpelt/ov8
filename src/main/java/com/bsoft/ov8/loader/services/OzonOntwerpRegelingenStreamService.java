package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.OntwerpRegelingDTO;
import com.bsoft.ov8.loader.mappers.OntwerpRegelingMapper;
import com.bsoft.ov8.loader.utils.OntwerpRegelingDTOSaver;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.presenteren.model.Ontwerpregeling;
import nl.overheid.omgevingswet.ozon.presenteren.model.Ontwerpregelingen;
import nl.overheid.omgevingswet.ozon.presenteren.model.OntwerpregelingenSort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Value("${api.ozon.presenteren.base-url}")
    private String ozonBaseUrl;

    public OzonOntwerpRegelingenStreamService(@Qualifier("ozonRegelingenWebClient") WebClient webClient,
                                              OntwerpRegelingDTOSaver ontwerpRegelingDTOSaver,
                                              OntwerpRegelingMapper ontwerpRegelingMapper

    ) {
        this.webClient = webClient;
        this.ontwerpRegelingDTOSaver = ontwerpRegelingDTOSaver;
        this.ontwerpRegelingMapper = ontwerpRegelingMapper;
    }

    /**
     * Build URI following the OpenAPI spec parameters
     */
    private String buildUri(
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<OntwerpregelingenSort> sort) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(ozonBaseUrl)
                .path("/ontwerpregelingen");

        // Add parameters following the OpenAPI spec
        Optional.ofNullable(beschikbaarOp)
                .ifPresent(val -> uriBuilder.queryParam("beschikbaarOp", val.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)));

        Optional.ofNullable(synchroniseerMetTileset)
                .ifPresent(val -> uriBuilder.queryParam("synchroniseerMetTileset", val));

        Optional.ofNullable(expand)
                .ifPresent(val -> uriBuilder.queryParam("_expand", val));

        Optional.ofNullable(page)
                .ifPresent(val -> uriBuilder.queryParam("page", val));

        Optional.ofNullable(size)
                .ifPresent(val -> uriBuilder.queryParam("size", val));

        // Handle sorting parameter
        if (sort != null && !sort.isEmpty()) {
            String sortString = sort.stream()
                    .map(OntwerpregelingenSort::name)
                    .collect(Collectors.joining(","));
            uriBuilder.queryParam("_sort", sortString);
        }

        return uriBuilder.build().toUriString();
    }

    /**
     * Get all ontwerpregelingen following the OpenAPI spec signature
     *
     * @param beschikbaarOp           Time travel parameter along the 'available' timeline
     * @param synchroniseerMetTileset Synchronize with tileset moment
     * @param expand                  Load related embedded resources
     * @param page                    Page number (minimum 1)
     * @param size                    Page size (minimum 1, maximum 200)
     * @param sort                    Sorting parameters
     * @return Mono containing the Ontwerpregelingen response
     */
    public Mono<Ontwerpregelingen> getOntwerpregelingen(
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<OntwerpregelingenSort> sort) {

        String uri = buildUri(beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Ontwerpregelingen.class)
                .doOnError(e -> log.error("Error fetching ontwerpregelingen from {}: {}", uri, e.getMessage()));
    }

    /**
     * Get all ontwerpregelingen across all pages as a Flux stream
     * This maintains your original streaming functionality while following the spec
     */
    public Flux<Ontwerpregeling> getAllOntwerpRegelingenStream(
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer initialPage,
            Integer size,
            List<OntwerpregelingenSort> sort) {

        // Start with the first page
        return getOntwerpregelingen(beschikbaarOp, synchroniseerMetTileset, expand, initialPage, size, sort)
                .expand(response -> {
                    // Check if there's a next page
                    if (response.getLinks() != null && response.getLinks().getNext() != null) {
                        String nextLink = response.getLinks().getNext().getHref().toString();
                        return fetchPageByUrl(nextLink);
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(this::extractOntwerpRegelingenFromResponse);
    }

    /**
     * Extract individual Ontwerpregeling objects from the API response
     */
    private Flux<Ontwerpregeling> extractOntwerpRegelingenFromResponse(Ontwerpregelingen response) {
        if (response.getEmbedded() != null && response.getEmbedded().getOntwerpregelingen() != null) {
            log.debug("Found {} ontwerpregelingen in response", response.getEmbedded().getOntwerpregelingen().size());
            return Flux.fromIterable(response.getEmbedded().getOntwerpregelingen());
        }
        return Flux.empty();
    }

    /**
     * Fetch a page by direct URL (for pagination)
     */
    private Mono<Ontwerpregelingen> fetchPageByUrl(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Ontwerpregelingen.class)
                .doOnError(e -> log.error("Error fetching page from {}: {}", url, e.getMessage()));
    }

    /**
     * Process all ontwerpregelingen with OpenAPI spec-compliant method signature
     */
    public void processAll(
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer initialPage,
            Integer size,
            List<OntwerpregelingenSort> sort) {

        final long start = System.currentTimeMillis();

        getAllOntwerpRegelingenStream(beschikbaarOp, synchroniseerMetTileset, expand, initialPage, size, sort)
                .doOnNext(ontwerpregeling -> {
                    log.debug("Processing ontwerpregeling {}", ontwerpregeling.toString());
                    try {
                        OntwerpRegelingDTO ontwerpRegelingDTO = ontwerpRegelingMapper.toOntwerpRegelingDTO(ontwerpregeling);
                        ontwerpRegelingDTOSaver.saveOntwerpregeling(ontwerpRegelingDTO, ontwerpregeling);
                    } catch (Exception e) {
                        log.error("Error processing ontwerpregeling {}: {}",
                                ontwerpregeling.getIdentificatie(), e.getMessage());
                    }
                })
                .doOnError(e -> log.error("Error during processing: {}", e.getMessage()))
                .doOnComplete(() -> {
                    log.info("All ontwerpregelingen processing complete. Duration: {} ms",
                            (System.currentTimeMillis() - start));
                })
                .subscribe();
    }

    /**
     * Process all with default parameters - convenience method
     */
    public void processAllWithDefaults(OffsetDateTime beschikbaarOp) {
        processAll(
                beschikbaarOp,
                null,           // synchroniseerMetTileset
                true,           // expand
                1,              // page
                200,            // size (max allowed)
                null            // sort (use default)
        );
    }

    /**
     * Get single page response (useful for testing or when you only need one page)
     */
    public Mono<Ontwerpregelingen> getSinglePage(
            OffsetDateTime beschikbaarOp,
            Integer page,
            Integer size) {

        return getOntwerpregelingen(beschikbaarOp, null, true, page, size, null);
    }

    /**
     * Count total available ontwerpregelingen (gets first page and checks total)
     */
    public Mono<Long> getTotalCount(OffsetDateTime beschikbaarOp) {
        return getSinglePage(beschikbaarOp, 1, 1)
                .map(response -> {
                    if (response.getPage() != null && response.getPage().getTotalElements() != null) {
                        return response.getPage().getTotalElements().longValue();
                    }
                    return 0L;
                })
                .onErrorReturn(0L);
    }
}