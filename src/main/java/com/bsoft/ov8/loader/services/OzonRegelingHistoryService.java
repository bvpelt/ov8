package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.controller.RegelingDTOSaver;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Slf4j
@Service
public class OzonRegelingHistoryService {

    private final WebClient webClient;
    private final RegelingRepository regelingRepository;
    private final RegelingMapper regelingMapper;
    private final RegelingDTOSaver regelingDTOSaver;

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    public OzonRegelingHistoryService(WebClient webClient,
                                      RegelingRepository regelingRepository,
                                      RegelingMapper regelingMapper,
                                      RegelingDTOSaver regelingDTOSaver) {
        this.webClient = webClient;
        this.regelingRepository = regelingRepository;
        this.regelingMapper = regelingMapper;
        this.regelingDTOSaver = regelingDTOSaver;
    }

    /**
     * Process all regelingen with version > 1 sequentially
     */
    public void processAll() {
        final long start = System.currentTimeMillis();
        retrieveAndSaveHistoricalRegelingen()
                .blockLast(); // Block to ensure completion

        log.info("Duration: " + (System.currentTimeMillis() - start));
    }

    /**
     * Retrieves RegelingDTOs from the database, fetches corresponding historical versions
     * from an external API, converts them, and saves them to the database SEQUENTIALLY.
     *
     * @return A Flux of RegelingDTOs that were successfully fetched and saved.
     */
    public Flux<RegelingDTO> retrieveAndSaveHistoricalRegelingen() {
        return Mono.fromCallable(() -> regelingRepository.findByVersieGreaterThan(1))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                // KEY CHANGE: Use concatMap instead of flatMap for sequential processing
                .concatMap(this::processRegelingSequentially)
                .onErrorContinue((throwable, obj) -> {
                    log.error("Error processing regeling: {}, error: {}", obj, throwable.getMessage());
                });
    }

    /**
     * Process a single regeling and all its historical versions sequentially
     */
    private Flux<RegelingDTO> processRegelingSequentially(RegelingDTO dbRegelingDTO) {
        log.info("0001 - Processing regeling: {}", dbRegelingDTO.toString());

        String uriIdentifier = dbRegelingDTO.getIdentificatie().replace("/", "_");

        // Calculate the dates for the previous version
        LocalDate finalGeldigOpDate = null;
        LocalDate finalInwerkingOpDate = null;

        if (dbRegelingDTO.getRegistratiegegevens() != null) {
            finalGeldigOpDate = (dbRegelingDTO.getRegistratiegegevens().getBeginGeldigheid() != null) ?
                    dbRegelingDTO.getRegistratiegegevens().getBeginGeldigheid().minusDays(1) : null;
            finalInwerkingOpDate = (dbRegelingDTO.getRegistratiegegevens().getBeginInwerking() != null) ?
                    dbRegelingDTO.getRegistratiegegevens().getBeginInwerking().minusDays(1) : null;
        }

        if (finalGeldigOpDate == null || finalInwerkingOpDate == null) {
            log.info("0002 - Skipping regeling {} due to missing dates.", dbRegelingDTO.getIdentificatie());
            return Flux.empty();
        }

        // Process all historical versions for this regeling sequentially
        return processHistoricalVersionsSequentially(
                uriIdentifier,
                dbRegelingDTO.getRegistratiegegevens().getVersie().intValue(),
                finalGeldigOpDate,
                finalInwerkingOpDate
        );
    }

    /**
     * Recursively fetch and save all historical versions sequentially
     */
    private Flux<RegelingDTO> processHistoricalVersionsSequentially(
            String uriIdentifier,
            Integer currentVersion,
            LocalDate geldigOpDate,
            LocalDate inwerkingOpDate) {

        log.info("0003 - processHistoricalVersionsSequentially version: {}, geldigOp: {}, inwerkingOp: {}, uri: {}", currentVersion, geldigOpDate, inwerkingOpDate, uriIdentifier);

        if (currentVersion <= 1) {
            // Base case: no more versions to process
            return Flux.empty();
        }

        log.info("0004 - Fetching historical version for {}, geldigOp: {}, inwerkingOp: {}",
                uriIdentifier, geldigOpDate, inwerkingOpDate);

        return fetchRegelingFromApi(uriIdentifier, geldigOpDate, inwerkingOpDate)
                .flatMap(apiRegeling -> {
                    log.info("0006 - Found historical version: {}", apiRegeling);

                    // Convert and save this version
                    RegelingDTO newRegelingDTO = convertToRegelingDTO(apiRegeling);
                    newRegelingDTO.setId(null); // Ensure new entity

                    return Mono.fromCallable(() -> {
                                // Check if this version already exists
                                String identificatie = newRegelingDTO.getIdentificatie();
                                LocalDate beginGeldigheid = newRegelingDTO.getRegistratiegegevens().getBeginGeldigheid();
                                OffsetDateTime registratieMoment = newRegelingDTO.getRegistratiegegevens().getTijdstipRegistratie();

                                boolean exists = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                                        identificatie, registratieMoment, beginGeldigheid).isPresent();

                                if (exists) {
                                    log.info("0007 - Version already exists, skipping save for {}", identificatie);
                                    return null; // Skip saving
                                }

                                RegelingDTO savedRegeling = regelingDTOSaver.saveRegeling(newRegelingDTO, apiRegeling);
//                                RegelingDTO savedRegeling = regelingRepository.save(newRegelingDTO);
                                log.info("0008 - Saved regeling id: {} identificatie: {} versie: {}",
                                        savedRegeling.getId(),
                                        savedRegeling.getIdentificatie(),
                                        savedRegeling.getRegistratiegegevens().getVersie());
                                return savedRegeling;
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .cast(RegelingDTO.class)
                .flux()
                .filter(dto -> dto != null) // Filter out null results from skipped saves
                .concatWith(
                        // SEQUENTIAL RECURSION: Process next historical version
                        Flux.defer(() -> {
                            // Calculate dates for the next (older) version
                            LocalDate nextGeldigOpDate = geldigOpDate.minusDays(1);
                            LocalDate nextInwerkingOpDate = inwerkingOpDate.minusDays(1);


                            return processHistoricalVersionsSequentially(
                                    uriIdentifier,
                                    currentVersion - 1,
                                    nextGeldigOpDate,
                                    nextInwerkingOpDate
                            );
                        })
                )
                .onErrorResume(e -> {
                    log.error("0007 Error processing historical version for {} at geldigOp {} , inwerkingOp {} : {}",
                            uriIdentifier, geldigOpDate, inwerkingOpDate, e.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Alternative approach: Process all versions using an iterative approach instead of recursion
     */
    private Flux<RegelingDTO> processHistoricalVersionsIteratively(
            String uriIdentifier,
            Integer maxVersion,
            LocalDate initialGeldigOpDate,
            LocalDate initialInwerkingOpDate) {

        return Flux.range(1, maxVersion - 1) // Generate versions from 1 to maxVersion-1
                .map(versionOffset -> maxVersion - versionOffset) // Process from highest to lowest
                .concatMap(version -> {
                    // Calculate dates for this version
                    LocalDate geldigOpDate = initialGeldigOpDate.minusDays(maxVersion - version);
                    LocalDate inwerkingOpDate = initialInwerkingOpDate.minusDays(maxVersion - version);

                    return fetchRegelingFromApi(uriIdentifier, geldigOpDate, inwerkingOpDate)
                            .flatMap(apiRegeling -> {
                                RegelingDTO newRegelingDTO = convertToRegelingDTO(apiRegeling);
                                newRegelingDTO.setId(null);

                                return Mono.fromCallable(() -> {
                                            String identificatie = newRegelingDTO.getIdentificatie();
                                            LocalDate beginGeldigheid = newRegelingDTO.getRegistratiegegevens().getBeginGeldigheid();
                                            OffsetDateTime registratieMoment = newRegelingDTO.getRegistratiegegevens().getTijdstipRegistratie();

                                            boolean exists = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                                                    identificatie, registratieMoment, beginGeldigheid).isPresent();

                                            if (exists) {
                                                log.info("Version already exists, skipping save for {}", identificatie);
                                                return null;
                                            }

                                            RegelingDTO savedRegeling = regelingRepository.save(newRegelingDTO);
                                            log.info("Saved regeling {} identificatie: {} versie: {}",
                                                    savedRegeling.getId(),
                                                    savedRegeling.getIdentificatie(),
                                                    savedRegeling.getRegistratiegegevens().getVersie());
                                            return savedRegeling;
                                        })
                                        .subscribeOn(Schedulers.boundedElastic());
                            })
                            .cast(RegelingDTO.class)
                            .flux()
                            .filter(dto -> dto != null)
                            .onErrorResume(e -> {
                                log.error("Error processing version {} for {}: {}",
                                        version, uriIdentifier, e.getMessage());
                                return Flux.empty();
                            });
                });
    }

    private RegelingDTO convertToRegelingDTO(Regeling apiRegeling) {
        return regelingMapper.toRegelingDTO(apiRegeling);
    }

    /**
     * Makes a reactive API call to retrieve a Regeling from the external service.
     */
    private Mono<Regeling> fetchRegelingFromApi(String uriIdentifier, LocalDate geldigOpDate, LocalDate inwerkingOpDate) {

        log.info("0005 - fetchRegelingFromApi geldigOp: {}, inwerkingOp: {}, uri: {}", geldigOpDate, inwerkingOpDate, uriIdentifier);

        String apiPath = String.format("/regelingen/%s", uriIdentifier);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(ozonBaseUrl)
                .path(apiPath)
                .queryParam("geldigOp", geldigOpDate.toString())
                .queryParam("inWerkingOp", inwerkingOpDate.toString());

        String uri = uriBuilder.build().toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Regeling.class)
                .doOnError(e -> log.error("API call error for {}: {}", uriIdentifier, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}