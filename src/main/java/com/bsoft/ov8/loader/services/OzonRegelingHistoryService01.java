package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.clients.OzonRegelingenClient;
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

@Slf4j
@Service
public class OzonRegelingHistoryService01 {

    private final OzonRegelingenClient ozonRegelingenClient;
    private final WebClient webClient;
    private final RegelingRepository regelingRepository;
    private final RegelingMapper regelingMapper;

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    public OzonRegelingHistoryService01(OzonRegelingenClient ozonRegelingenClient,
                                        WebClient webClient,
                                        RegelingRepository regelingRepository,
                                        RegelingMapper regelingMapper) {
        this.ozonRegelingenClient = ozonRegelingenClient;
        this.webClient = webClient;
        this.regelingRepository = regelingRepository;
        this.regelingMapper = regelingMapper;
    }

    /**
     * To proces history
     * - find all regelingen with version > 1
     * -- for each regeling r
     * --- get r.versie
     * --- while r.versie > 1
     * --- get previous regeling p with p.identificatie = r.identifiatie and p.geldigheid = r.geldigheid -1 and p.inwerking = r.inwerking - 1
     * --- if regeling does not exist
     * ---- save regeling
     */
    public void processAll() {
        retrieveAndSaveHistoricalRegelingen()
                .subscribe();
    }


    /**
     * Retrieves RegelingDTOs from the database, fetches corresponding historical versions
     * from an external API, converts them, and saves them to the database reactively.
     *
     * @return A Flux of RegelingDTOs that were successfully fetched and saved.
     */
    /**
     * Retrieves RegelingDTOs from the database, fetches corresponding historical versions
     * from an external API, converts them, and saves them to the database reactively.
     *
     * @return A Flux of RegelingDTOs that were successfully fetched and saved.
     */
    public Flux<RegelingDTO> retrieveAndSaveHistoricalRegelingen() {
        return Mono.fromCallable(() -> regelingRepository.findByVersieGreaterThan(1))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .flatMap(dbRegelingDTO -> {
                    log.info("0001 Processing regeling: {}", dbRegelingDTO.toString());
                    String uriIdentifier = dbRegelingDTO.getIdentificatie().replace("/", "_");

                    // Use effectively final variables for dates
                    final LocalDate finalGeldigOpDate;
                    final LocalDate finalInwerkingOpDate;

                    if (dbRegelingDTO.getRegistratiegegevens() != null) {
                        finalGeldigOpDate = (dbRegelingDTO.getRegistratiegegevens().getBeginGeldigheid() != null) ?
                                dbRegelingDTO.getRegistratiegegevens().getBeginGeldigheid().minusDays(1) : null;
                        finalInwerkingOpDate = (dbRegelingDTO.getRegistratiegegevens().getBeginInwerking() != null) ?
                                dbRegelingDTO.getRegistratiegegevens().getBeginInwerking().minusDays(1) : null;
                    } else {
                        finalGeldigOpDate = null;
                        finalInwerkingOpDate = null;
                    }

                    if (finalGeldigOpDate == null || finalInwerkingOpDate == null) {
                        log.info("0002 Skipping regeling {} due to missing dates.", dbRegelingDTO.getIdentificatie());
                        return Mono.empty();
                    }

                    return fetchRegelingFromApi(uriIdentifier, finalGeldigOpDate, finalInwerkingOpDate)
                            .flatMap(apiRegeling -> {
                                log.info("0003 apiRegeling: {}", apiRegeling);
                                RegelingDTO newRegelingDTO = convertToRegelingDTO(apiRegeling);
                                log.info("0004 newRegelingDTO: {}", newRegelingDTO);
                                newRegelingDTO.setId(null);

                                return Mono.fromCallable(() -> {
                                            RegelingDTO savedRegeling = regelingRepository.save(newRegelingDTO);
                                            log.info("0005 Saved regeling {} identificatie: {} versie: {}", savedRegeling.getId(), savedRegeling.getIdentificatie(), savedRegeling.getRegistratiegegevens().getVersie());
                                            return savedRegeling;
                                        })
                                        .subscribeOn(Schedulers.boundedElastic());
                            })
                            .onErrorResume(e -> {
                                // Use the effectively final date variables here
                                log.error("0006 Error fetching or saving regeling for ID {} at geldigOp {} , inwerkingOp {} : {}",
                                        dbRegelingDTO.getIdentificatie(), finalGeldigOpDate, finalInwerkingOpDate, e.getMessage());
                                return Mono.empty();
                            });
                });
    }

    private RegelingDTO convertToRegelingDTO(Regeling apiRegeling) {
        return regelingMapper.toRegelingDTO(apiRegeling);
    }

    /**
     * Makes a reactive API call to retrieve a Regeling from the external service.
     * Assumes WebClient is configured with the base URL (e.g., api.ozon.base-url).
     *
     * @param uriIdentifier   The identificatie with '/' replaced by '_'.
     * @param geldigOpDate    The geldigOp date to use in the API query.
     * @param inwerkingOpDate The inwerkingOp date to use in the API query.
     * @return A Mono that emits the Regeling object from the API, or an empty Mono if not found/error.
     */
    private Mono<Regeling> fetchRegelingFromApi(String uriIdentifier, LocalDate geldigOpDate, LocalDate inwerkingOpDate) {
        String apiPath = String.format("/regelingen/%s", uriIdentifier);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(ozonBaseUrl)
                .path(apiPath)
                .queryParam("geldigOp", geldigOpDate.toString())
                .queryParam("inWerkingOp", inwerkingOpDate.toString());

        String uri = uriBuilder.build().toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Regeling.class) // Assuming 'Regeling' is your POJO mapping the API response
                .doOnError(e -> log.error("API call error for {}: {}", uriIdentifier, e.getMessage()))
                .onErrorResume(e -> Mono.empty()); // Return empty Mono on API error (e.g., 404, network issue)
    }

}
