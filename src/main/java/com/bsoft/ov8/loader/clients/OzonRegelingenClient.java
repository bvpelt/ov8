package com.bsoft.ov8.loader.clients; // Ensure this package name is consistent

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import com.bsoft.ov8.loader.exceptions.OzonApiException;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.BevoegdGezagRepository;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import com.bsoft.ov8.loader.repositories.SoortRegelingRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.api.RegelingenApi;
import nl.overheid.omgevingswet.ozon.model.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class OzonRegelingenClient implements RegelingenApi {

    private final WebClient ozonWebClient;

    private final RegelingMapper regelingMapper;

    private final BevoegdGezagRepository bevoegdGezagRepository;

    private final RegelingRepository regelingRepository;

    private final SoortRegelingRepository soortRegelingRepository;

    public OzonRegelingenClient(WebClient ozonWebClient,
                                RegelingMapper regelingMapper,
                                BevoegdGezagRepository bevoegdGezagRepository,
                                RegelingRepository regelingRepository,
                                SoortRegelingRepository soortRegelingRepository
    ) {
        this.ozonWebClient = ozonWebClient;
        this.regelingMapper = regelingMapper;
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.regelingRepository = regelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
    }

    @Override
    public Mono<ResponseEntity<Regelingen>> _getRegelingen(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<RegelingenSort> sort, // Note: Use RegelingenApi.RegelingenSort for clarity
            ServerWebExchange exchange // This parameter is now part of the interface method
    ) {
        // You can log or ignore the 'exchange' parameter as it's not used for outgoing calls
        log.debug("Ignoring ServerWebExchange parameter for outgoing Ozon API call.");

        return ozonWebClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/regelingen");
                    if (geldigOp != null)
                        uriBuilder.queryParam("geldigOp", geldigOp.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    if (inWerkingOp != null)
                        uriBuilder.queryParam("inWerkingOp", inWerkingOp.format(DateTimeFormatter.ISO_LOCAL_DATE));
                    if (beschikbaarOp != null)
                        uriBuilder.queryParam("beschikbaarOp", beschikbaarOp.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                    if (synchroniseerMetTileset != null)
                        uriBuilder.queryParam("synchroniseerMetTileset", synchroniseerMetTileset);
                    if (expand != null) uriBuilder.queryParam("_expand", expand);
                    if (page != null) uriBuilder.queryParam("page", page);
                    if (size != null) uriBuilder.queryParam("size", size);
                    if (sort != null && !sort.isEmpty()) {
                        uriBuilder.queryParam("_sort", sort.stream()
                                .map(RegelingenSort::getValue) // Assuming getValue() if it's an enum
                                .collect(java.util.stream.Collectors.joining(",")));
                    }
                    return uriBuilder.build();
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(Problem400.class)
                                .doOnNext(problem -> log.error("Client error (4xx) from Ozon API: Status {} - {}", clientResponse.statusCode().value(), problem.getTitle()))
                                .flatMap(problem -> Mono.error(
                                        new OzonApiException("Client error from Ozon API: " + problem.getTitle(),
                                                clientResponse.statusCode(),
                                                problem.toString())) // Pass problem details
                                ))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(Problem.class)
                                .doOnNext(problem -> log.error("Server error (5xx) from Ozon API: Status {} - {}", clientResponse.statusCode().value(), problem.getTitle()))
                                .flatMap(problem -> Mono.error(
                                        new OzonApiException("Server error from Ozon API: " + problem.getTitle(),
                                                clientResponse.statusCode(),
                                                problem.toString())) // Pass problem details
                                ))
                // Add a default error handler for any other unhandled status codes (e.g., non-2xx that aren't 4xx/5xx)
                // or if Problem/Problem400 deserialization fails.
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class) // Try to get raw body if Problem deserialization fails
                                .doOnNext(errorBody -> log.error("Unhandled error status from Ozon API: Status {} - Body: {}", clientResponse.statusCode().value(), errorBody))
                                .flatMap(errorBody -> Mono.error(
                                        new OzonApiException("Unhandled error from Ozon API: Status " + clientResponse.statusCode().value(),
                                                clientResponse.statusCode(),
                                                errorBody)))
                )
                .toEntity(Regelingen.class)
                .doOnError(e -> {
                    // This doOnError will catch any exception *before* it's handled by a subscriber
                    // This includes network errors, JSON decoding errors (if not handled by onStatus bodyToMono),
                    // or the OzonApiException instances thrown above.
                    log.error("An unexpected error occurred while fetching regelingen from Ozon: {}", e.getMessage(), e);
                    // Crucially, we DO NOT 'throw e' here.
                    // The error signal is already flowing.
                });
    }

    /**
     * Fetches Regelingen and extracts individual Regeling objects into a Flux for processing.
     * Handles pagination if you're calling this repeatedly for different pages.
     */
    public Flux<Regeling> getIndividualRegelingenFlux(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<RegelingenSort> sort,
            ServerWebExchange exchange // Pass through if needed by _getRegelingen
    ) {
        return _getRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort, exchange)
                .flatMapMany(responseEntity -> {
                    // Check if the response is successful first
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        // If _getRegelingen didn't throw an OzonApiException (e.g., in a doOnError),
                        // we can still propagate an error here.
                        HttpStatusCode statusCode = responseEntity.getStatusCode();
                        String errorMessage = String.format("API call for regelingen failed with status %d", statusCode.value());
                        log.error(errorMessage);
                        return Flux.error(new OzonApiException(errorMessage, statusCode, null));
                    }

                    Regelingen regelingenWrapper = responseEntity.getBody();

                    // Safely extract the list of regelingen
                    List<Regeling> regelingList = Objects.requireNonNullElseGet(
                            regelingenWrapper != null && regelingenWrapper.getEmbedded() != null ?
                                    regelingenWrapper.getEmbedded().getRegelingen() : null,
                            List::of // If any part is null, return an empty list
                    );

                    // Convert the List<Regeling> into a Flux<Regeling>
                    return Flux.fromIterable(regelingList);
                })
                .doOnError(e -> log.error("Error processing individual regelingen: {}", e.getMessage(), e));
    }

    // Example of how you might use getIndividualRegelingenFlux in a service
    // (This is illustrative, your actual usage might differ)
    public Mono<Void> processAllRegelingen(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            // Assuming you want to iterate through multiple pages
            int startPage,
            int endPage,
            int pageSize,
            List<RegelingenSort> sort,
            ServerWebExchange exchange
    ) {
        // Create a Flux of page numbers
        return Flux.range(startPage, endPage - startPage + 1)
                // Use flatMap to make the API call for each page
                // Limit concurrency to avoid the "Pending acquire queue" error
                .flatMap(page -> getIndividualRegelingenFlux(
                                geldigOp,
                                inWerkingOp,
                                beschikbaarOp,
                                synchroniseerMetTileset,
                                expand,
                                page, // Pass the current page number
                                pageSize,
                                sort,
                                exchange
                        )
                                .doOnNext(regeling -> {
                                    log.info("Processing regeling from page {}: {}", page, regeling.getIdentificatie());
                                    log.info("regeling: {}", regeling);
                                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                                    log.trace("regelingDTO: {}", regelingDTO.toString());
                                    saveRegeling(regelingDTO);
                                }), // Example processing
                        5) // Limit to 5 concurrent page fetches
                .onErrorResume(OzonApiException.class, e -> {
                    log.error("Failed to fetch regelingen from Ozon API: {} - Status: {}", e.getMessage(), e.getStatusCode());
                    return Mono.empty(); // Or Mono.error(e) if you want to propagate the error further
                })
                .then() // This collects the results of the flatMap and returns Mono<Void>
                .doOnSuccess(voidResult -> log.info("Ready processing")) // This will execute when the entire stream completes successfully
                .doOnError(throwable -> log.error("An error occurred after processing: {}", throwable.getMessage())) // Optional: log if an error happened at the very end
                .then(); // Ensure the final return type is Mono<Void>
    }

    private Mono<Void> processSingleRegeling(Regeling regeling) {
        log.info("Processing Regeling: {}", regeling.getIdentificatie());
        // Add your actual processing logic here, e.g.:
        // - Save to database
        // - Send to another service
        // - Apply business rules
        return Mono.delay(Duration.ofMillis(100)) // Simulate some async work
                .then();
    }

    /**
     * This method fetches a page of Regelingen and then extracts and processes
     * each individual Regeling from the embedded list.
     * It returns a Flux<Regeling> to allow for streaming and processing each item.
     */
    public Flux<Regeling> getAndProcessIndividualRegelingen(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<RegelingenSort> sort
    ) {
        return _getRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort, null)
                // Use flatMapMany to transform the Mono<ResponseEntity<Regelingen>> into a Flux<Regeling>
                .flatMapMany(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        Regelingen wrapper = responseEntity.getBody();
                        if (wrapper != null && wrapper.getEmbedded() != null && wrapper.getEmbedded().getRegelingen() != null) {
                            return Flux.fromIterable(wrapper.getEmbedded().getRegelingen());
                        } else {
                            log.warn("Received successful response but body or embedded regelingen were null/empty for page {}.", page);
                            return Flux.empty(); // No regelingen found for this page
                        }
                    } else {
                        // This else block might be redundant if onStatus handles all errors,
                        // but it's good for clarity if an error somehow slips through or if
                        // you want different handling for a non-2xx response that isn't
                        // necessarily an "error" you want to throw an exception for yet.
                        log.warn("Received non-2xx status code {}. Not processing regelingen for this page.", responseEntity.getStatusCode().value());
                        return Flux.empty(); // Or Flux.error(new RuntimeException("Bad status")) if you want to propagate an error here.
                    }
                })
                .doOnNext(regeling -> {
                    // This is where you process EACH individual Regeling
                    log.info("Processing Regeling with ID: {}", regeling.getIdentificatie());
                    log.info("Processing individual Regeling id: {}", regeling.getIdentificatie());
                    log.info("regeling: {}", regeling);
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    log.trace("regelingDTO: {}", regelingDTO.toString());
                    saveRegeling(regelingDTO);
                })
                .doOnError(e -> {
                    // This doOnError catches any error that occurred upstream in this Flux pipeline.
                    // This includes OzonApiException (from _getRegelingen) or any error
                    // during the flatMapMany transformation (e.g., NullPointerException if you had bad logic).
                    log.error("Error processing individual regelingen for page {}: {}", page, e.getMessage(), e);
                    // The error will propagate to the subscriber of this Flux
                });
    }

    //@Transactional
    public void saveRegeling(RegelingDTO regelingDTO) {

        String bevoegdGezagCode;
        Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO;
        BevoegdGezagDTO savedBevoegdGezagDTO = null;

        Optional<RegelingDTO> optionalRegelingDTO = regelingRepository.findByIdentificatieAndTijdstipRegistratieAndBeginGeldigheid(regelingDTO.getIdentificatie(), regelingDTO.getTijdstipRegistratie(), regelingDTO.getBeginGeldigheid());

        if (optionalRegelingDTO.isEmpty()) {
            if (regelingDTO.getBevoegdGezag() != null) {
                bevoegdGezagCode = regelingDTO.getBevoegdGezag().getCode();
                optionalBevoegdGezagDTO = bevoegdGezagRepository.findByCode(bevoegdGezagCode);

                if (!optionalBevoegdGezagDTO.isPresent()) {
                    savedBevoegdGezagDTO = bevoegdGezagRepository.save(regelingDTO.getBevoegdGezag());
                } else {
                    savedBevoegdGezagDTO = optionalBevoegdGezagDTO.get();
                }
                regelingDTO.setBevoegdGezag(savedBevoegdGezagDTO);
            }

            String soortRegelingCode;
            Optional<SoortRegelingDTO> optionalSoortRegelingDTO;
            SoortRegelingDTO savedSoortRegelingDTO = null;
            if (regelingDTO.getType() != null) {
                soortRegelingCode = regelingDTO.getType().getCode();
                optionalSoortRegelingDTO = soortRegelingRepository.findByCode(soortRegelingCode);

                if (!optionalSoortRegelingDTO.isPresent()) {
                    savedSoortRegelingDTO = soortRegelingRepository.save(regelingDTO.getType());
                } else {
                    savedSoortRegelingDTO = optionalSoortRegelingDTO.get();
                }
                regelingDTO.setType(savedSoortRegelingDTO);
            }

            regelingRepository.save(regelingDTO);
        } else {
            log.info("Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} exists", regelingDTO.getIdentificatie(), regelingDTO.getTijdstipRegistratie(), regelingDTO.getBeginGeldigheid());
        }
    }
}