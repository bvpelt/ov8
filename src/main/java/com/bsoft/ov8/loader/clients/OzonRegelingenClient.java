package com.bsoft.ov8.loader.clients; // Ensure this package name is consistent

import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import com.bsoft.ov8.loader.exceptions.OzonApiException;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.BevoegdGezagRepository;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import com.bsoft.ov8.loader.repositories.SoortRegelingRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.api.RegelingenApi;
import nl.overheid.omgevingswet.ozon.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.Spliterators.iterator;

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

    // --- Convenience method for your application (recommended usage) ---
    // This method does NOT include ServerWebExchange, making it easier to call from your services.
    public Flux<Regeling> getIndividualRegelingen(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<RegelingenSort> sort
    ) {
        // When calling _getRegelingen, just pass null for the ServerWebExchange parameter
        return _getRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort, null)
                .flatMapMany(responseEntity -> {

                    // Check for non-successful status codes that might not be caught by onStatus() yet
                    if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                        HttpStatusCode statusCode = responseEntity.getStatusCode();
                        String reasonPhrase;
                        if (statusCode instanceof HttpStatus httpStatus) { // Use pattern matching for instanceof
                            reasonPhrase = httpStatus.getReasonPhrase();
                        } else {
                            reasonPhrase = "Unknown Status"; // Fallback for custom HttpStatusCode implementations
                        }

                        String errorMessage = String.format("Received non-successful response from Ozon API: %s %s",
                                statusCode.value(), reasonPhrase);
                        log.error(errorMessage);
                        // Propagate a more specific exception if the status is an error
                        return Flux.error(new WebClientResponseException(
                                statusCode.value(),
                                reasonPhrase, // Use the resolved reasonPhrase
                                null, // Headers, if needed
                                null, // Body as byte[], if needed
                                null // Charset, if needed
                        ));
                    }

                    if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                        Regelingen wrapper = responseEntity.getBody();
                        Page curpage = wrapper.getPage();

                        if (wrapper.getEmbedded() != null && wrapper.getEmbedded().getRegelingen() != null) {
                            return Flux.fromIterable(wrapper.getEmbedded().getRegelingen());
                        }
                    }
                    log.warn("Received non-successful response or empty body for regelingen: {}", responseEntity.getStatusCode());
                    return Flux.empty();
                })
                .doOnError(e -> {
                    log.error("Error processing individual regelingen: {}", e.getMessage());
                });
    }

    // Example of how to use the convenience method (e.g., from a service or controller)
    public void processEachRegeling(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            Integer page,
            Integer size,
            List<RegelingenSort> sort
    ) {
        getIndividualRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort)
                .doOnNext(regeling -> {
                    log.info("Processing individual Regeling id: {}", regeling.getIdentificatie());
                    log.info("regeling: {}", regeling.toString());
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    log.trace("regelingDTO: {}", regelingDTO.toString());
                    saveRegeling(regelingDTO);
                })
                .doOnError(e -> log.error("Error during processing individual Regeling: {}", e.getMessage()))
                .subscribe();
    }

    @Transactional
    public void saveRegeling(RegelingDTO regelingDTO) {

        String bevoegdGezagCode;
        Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO;
        BevoegdGezagDTO savedBevoegdGezagDTO = null;

        Optional<RegelingDTO> optionalRegelingDTO = regelingRepository.findByIdentificatieAndTijdstipRegistratieAndBeginGeldigheid(regelingDTO.getIdentificatie().toString(), regelingDTO.getTijdstipRegistratie(), regelingDTO.getBeginGeldigheid());

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
            log.info("Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} exists", regelingDTO.getIdentificatie().toString(), regelingDTO.getTijdstipRegistratie(), regelingDTO.getBeginGeldigheid());
        }
    }
}