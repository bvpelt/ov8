package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import nl.overheid.omgevingswet.ozon.model.Regelingen;
import nl.overheid.omgevingswet.ozon.model.RegelingenSort;
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
public class OzonRegelingenStreamService {

    private final WebClient webClient;
    private final RegelingMapper regelingMapper;
    private final RegelingDTOSaver regelingDTOSaver;

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    public OzonRegelingenStreamService(WebClient webClient,
                                       RegelingDTOSaver regelingDTOSaver,
                                       RegelingMapper regelingMapper

    ) {
        this.webClient = webClient;
        this.regelingDTOSaver = regelingDTOSaver;
        this.regelingMapper = regelingMapper;
    }

    /**
     * Fetches all regelingen by traversing all pages provided by the API.
     *
     * @param geldigOp      The 'geldigOp' date.
     * @param inWerkingOp   The 'inWerkingOp' date.
     * @param beschikbaarOp The 'beschikbaarOp' datetime.
     * @param initialPage   Initial page number (can be null for default 1).
     * @param size          Page size (can be null for default 20).
     * @param sort          List of sorting parameters.
     * @param fields        Optional fields to include.
     * @return A Flux of all Regelingen across all pages.
     */
    public Flux<Regeling> getAllRegelingen( // Changed return type to Flux<Regeling>
                                            LocalDate geldigOp,
                                            LocalDate inWerkingOp,
                                            OffsetDateTime beschikbaarOp,
                                            Boolean _expand,
                                            String inwerkingTot,
                                            String geldigTot,
                                            Integer initialPage,
                                            Integer size,
                                            List<RegelingenSort> sort,
                                            String fields
    ) {
        String initialUri = buildInitialUri(
                geldigOp,
                inWerkingOp,
                beschikbaarOp,
                _expand,
                inwerkingTot,
                geldigTot,
                initialPage,
                size,
                sort,
                fields
        );

        return fetchPage(initialUri) // Use the helper method here
                .expand(response -> { // 'response' is now of type Regelingen
                    if (response.getLinks() != null && response.getLinks().getNext() != null) {
                        String nextLink = response.getLinks().getNext().getHref().toString(); // Get URI and convert to String
                        return fetchPage(nextLink); // Fetch the next page
                    } else {
                        return Mono.empty();
                    }
                })
                .flatMap(regelingenResponse -> { // 'regelingenResponse' is now of type Regelingen
                    if (regelingenResponse.getEmbedded() != null && regelingenResponse.getEmbedded().getRegelingen() != null) {
                        // Assuming RegelingenEmbedded has a getRegelingen() method that returns List<Regeling>
                        return Flux.fromIterable(regelingenResponse.getEmbedded().getRegelingen());
                    }
                    return Flux.empty();
                });
    }

    /**
     * Helper method to fetch a single page of regelingen.
     */
    private Mono<Regelingen> fetchPage(String uri) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Regelingen.class) // Correct type here
                .doOnError(e -> System.err.println("Error fetching page " + uri + ": " + e.getMessage()));
    }

    /**
     * Helper method to build the initial URI with query parameters.
     */
    private String buildInitialUri(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            Boolean _expand,
            String inwerkingTot,
            String geldigTot,
            Integer page,
            Integer size,
            List<RegelingenSort> sort,
            String fields
    ) {
        // Define the base URL for the external service
        String baseUrl = ozonBaseUrl;

        // Use UriComponentsBuilder directly to construct the URI
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/regelingen")
                .queryParam("geldigOp", geldigOp.toString())
                .queryParam("inWerkingOp", inWerkingOp.toString())
                .queryParam("beschikbaarOp", beschikbaarOp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        // Add optional parameters using queryParamIfPresent for robustness
        Optional.ofNullable(inwerkingTot).ifPresent(val -> uriBuilder.queryParam("inwerkingTot", val));
        Optional.ofNullable(geldigTot).ifPresent(val -> uriBuilder.queryParam("geldigTot", val));
        Optional.ofNullable(page).ifPresent(val -> uriBuilder.queryParam("page", val));
        Optional.ofNullable(size).ifPresent(val -> uriBuilder.queryParam("size", val));
        Optional.ofNullable(_expand).ifPresent(val -> uriBuilder.queryParam("_expand", val));

        // Handle the 'sort' parameter, joining multiple if needed
        if (sort != null && !sort.isEmpty()) {
            String sortString = sort.stream()
                    .map(RegelingenSort::name)
                    .collect(Collectors.joining(","));
            uriBuilder.queryParam("_sort", sortString);
        }

        Optional.ofNullable(fields).ifPresent(val -> uriBuilder.queryParam("fields", val));

        // Build and return the URI as a String
        return uriBuilder.build().toUriString();
    }

    public void procesAll(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            Boolean _expand,
            String inwerkingTot,
            String geldigTot,
            Integer initialPage,
            Integer size,
            List<RegelingenSort> sort,
            String fields
    ) {
        final long start = System.currentTimeMillis();

        getAllRegelingen(geldigOp, inWerkingOp, beschikbaarOp, _expand, inwerkingTot, geldigTot, initialPage, size, sort, fields)
                .doOnNext(regeling -> {
                    log.debug("Processing regeling {}", regeling.getIdentificatie());
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    regelingDTOSaver.saveRegeling(regelingDTO, regeling);
                })
                .doOnError(e -> log.error("Error during processing individual Regeling: {}", e.getMessage()))
                .doOnComplete(() -> {
                    log.debug("All regelingen processing complete");
                    log.info("Duration: " + (System.currentTimeMillis() - start));
                })
                .subscribe();
    }

}