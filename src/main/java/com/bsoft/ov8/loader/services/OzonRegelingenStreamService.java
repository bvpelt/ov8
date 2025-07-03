package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.clients.OzonRegelingenClient;
import com.bsoft.ov8.loader.database.*;
import com.bsoft.ov8.loader.mappers.BoundingBoxMapper;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.mappers.RegistratieGegevensMapper;
import com.bsoft.ov8.loader.repositories.BevoegdGezagRepository;
import com.bsoft.ov8.loader.repositories.LocatieRepository;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import com.bsoft.ov8.loader.repositories.SoortRegelingRepository;
import nl.overheid.omgevingswet.ozon.model.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OzonRegelingenStreamService {

    private final OzonRegelingenClient ozonRegelingenClient;
    private final WebClient webClient;
    private final BoundingBoxMapper boundingBoxMapper;
    private final RegelingMapper regelingMapper;
    private final RegistratieGegevensMapper registratieGegevensMapper;
    private final BevoegdGezagRepository bevoegdGezagRepository;
    private final RegelingRepository regelingRepository;
    private final SoortRegelingRepository soortRegelingRepository;
    private final LocatieRepository locatieRepository;

    @Value("${api.ozon.base-url}")
    private String ozonBaseUrl;

    public OzonRegelingenStreamService(OzonRegelingenClient ozonRegelingenClient,
                                       WebClient webClient,
                                       BoundingBoxMapper boundingBoxMapper,
                                       RegelingMapper regelingMapper,
                                       RegistratieGegevensMapper registratieGegevensMapper,
                                       BevoegdGezagRepository bevoegdGezagRepository,
                                       RegelingRepository regelingRepository,
                                       SoortRegelingRepository soortRegelingRepository,
                                       LocatieRepository locatieRepository
    ) {
        this.ozonRegelingenClient = ozonRegelingenClient;
        this.webClient = webClient;
        this.boundingBoxMapper = boundingBoxMapper;
        this.regelingMapper = regelingMapper;
        this.registratieGegevensMapper = registratieGegevensMapper;
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.regelingRepository = regelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
        this.locatieRepository = locatieRepository;
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
                    log.info("Processing regeling {}", regeling.getIdentificatie());
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    saveRegeling(regelingDTO, regeling);
                })
                .doOnError(e -> log.error("Error during processing individual Regeling: {}", e.getMessage()))
                .doOnComplete(() -> {
                    log.info("All regelingen processing complete");
                    log.info("Duration: " + (System.currentTimeMillis() - start));
                })
                .subscribe();
    }

    @Transactional
    public void saveRegeling(RegelingDTO regelingDTO, Regeling regeling) {
        log.info("===> Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {}.",
                regelingDTO.getIdentificatie(),
                regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

        oneToMany(regelingDTO);

        // Check if RegelingDTO already exists
        Optional<RegelingDTO> optionalRegelingDTO = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                regelingDTO.getIdentificatie(),
                regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

        if (optionalRegelingDTO.isEmpty()) {
            log.info("+++> New Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} not exists. Saving regeling.",
                    regelingDTO.getIdentificatie(),
                    regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                    regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

            regelingRepository.save(regelingDTO);

            manyToOne(regelingDTO, regeling);

            // --- Save the RegelingDTO ---
            // Hibernate will now correctly manage the many-to-many relationship
            // based on the managed entities in regelingDTO.regelingsgebied and the cascade type.
            regelingRepository.save(regelingDTO);

        } else {
            log.info("---> Existing Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} exists. Skipping save for now. <---",
                    regelingDTO.getIdentificatie(),
                    regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                    regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

            manyToOne(regelingDTO, regeling);
            regelingRepository.save(regelingDTO);
        }
    }

    private void manyToOne(RegelingDTO regelingDTO, Regeling regeling) {
        //
        // --- Prepare RelatieDTO's for Many-to-Many relationship opvolgervan ---
        Set<RegelingDTO> managedOpvolgerForRegeling = new HashSet<>();

        if (regeling.getOpvolgerVan() != null && !regeling.getOpvolgerVan().isEmpty()) {
            // Iterate over the Regeling mapped from the API response
            for (Regeling currentOpvolgerInRegeling : regeling.getOpvolgerVan()) {
                Optional<RegelingDTO> optionalOpvolgerVanDTO = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(currentOpvolgerInRegeling.getIdentificatie().toString(),
                        currentOpvolgerInRegeling.getGeregistreerdMet().getTijdstipRegistratie(),
                        currentOpvolgerInRegeling.getGeregistreerdMet().getBeginGeldigheid());

                RegelingDTO managedOpvolgerVanDTO;
                if (optionalOpvolgerVanDTO.isPresent()) {
                    // If LocatieDTO already exists, use the one from the database directly.
                    // It is already a managed entity within this transaction.
                    managedOpvolgerVanDTO = optionalOpvolgerVanDTO.get();
                } else {
                    // If RegelingDTO does not exist, save the new one to make it managed.
                    RegelingDTO opvolgerDTO = regelingMapper.toRegelingDTO(currentOpvolgerInRegeling);
                    managedOpvolgerVanDTO = regelingRepository.save(opvolgerDTO);
                }
                managedOpvolgerForRegeling.add(managedOpvolgerVanDTO);
            }
        }
        // Set the collection of managed RelatieDTO on the RegelingDTO (the owning side)
        regelingDTO.setOpvolgerVan(managedOpvolgerForRegeling);

        // --- Prepare LocatieDTOs for Many-to-Many relationship regelingsgebied ---
        Set<LocatieDTO> managedLocatiesForRegeling = new HashSet<>();
        RegelingAllOfEmbedded regelingAllOfEmbedded = regeling.getEmbedded();
        if (regelingAllOfEmbedded != null) {
            EmbeddedLocatie embeddedLocatie = regelingAllOfEmbedded.getRegelingsgebied();
            if (embeddedLocatie != null) {
                LocatieDTO locatieDTO = new LocatieDTO();
                locatieDTO.setIdentificatie(embeddedLocatie.getIdentificatie().toString());
                locatieDTO.setGeometrieIdentificatie(embeddedLocatie.getGeometrieIdentificatie());
                Optional<LocatieDTO> optionalLocatieDTO = locatieRepository.findByIdentificatieAndGeometrieIdentificatie(
                        locatieDTO.getIdentificatie(),
                        locatieDTO.getGeometrieIdentificatie());

                LocatieDTO managedLocatieDTO;
                if (optionalLocatieDTO.isPresent()) {
                    // If LocatieDTO already exists, use the one from the database directly.
                    // It is already a managed entity within this transaction.
                    managedLocatieDTO = optionalLocatieDTO.get();
                } else {
                    // If LocatieDTO does not exist, save the new one to make it managed.
                    locatieDTO.setLocatieType(embeddedLocatie.getLocatieType());
                    locatieDTO.setNoemer(embeddedLocatie.getNoemer());

                    BoundingBoxDTO boundingBoxDTO = boundingBoxMapper.toBoundingBoxDTO(embeddedLocatie.getBoundingBox());
                    locatieDTO.setBoundingBox(boundingBoxDTO);

                    RegistratiegegevensDTO registratiegegevensDTO = registratieGegevensMapper.toRegistratiegegevensDTO(embeddedLocatie.getGeregistreerdMet());

                    locatieDTO.setRegistratiegegevens(registratiegegevensDTO);

                    locatieDTO.setRegelingsgebieden(new HashSet<>());

                    managedLocatieDTO = locatieRepository.save(locatieDTO);

                    Set<LocatieDTO> managedEmbeddedLocatieDTO = new HashSet<>();
                    //
                    // Een gebiedengroep omvat 1..n locaties
                    if (embeddedLocatie.getLocatieType().getValue().equals("GEBIEDENGROEP")) {
                        List<EmbeddedLocatie> omvat = embeddedLocatie.getEmbedded().getOmvat();
                        omvat.forEach(gebied -> {
                            LocatieDTO omvatDTO = new LocatieDTO();
                            omvatDTO.setLocatieType(gebied.getLocatieType());
                            omvatDTO.setNoemer(gebied.getNoemer());

                            BoundingBoxDTO boundingBoxOmvatDTO = boundingBoxMapper.toBoundingBoxDTO(gebied.getBoundingBox());
                            omvatDTO.setBoundingBox(boundingBoxOmvatDTO);

                            RegistratiegegevensDTO registratiegegevensOmvatDTO = registratieGegevensMapper.toRegistratiegegevensDTO(gebied.getGeregistreerdMet());

                            omvatDTO.setRegistratiegegevens(registratiegegevensOmvatDTO);

                            omvatDTO.setRegelingsgebieden(new HashSet<>());

                            managedEmbeddedLocatieDTO.add(locatieRepository.save(omvatDTO));
                        });

                        managedLocatieDTO.setRelatieGebiedenGroep-Omvat()
                    }
                }
                managedLocatiesForRegeling.add(managedLocatieDTO);
            }

        }
        regelingDTO.setRegelingsgebied(managedLocatiesForRegeling);
    }

    private void oneToMany(RegelingDTO regelingDTO) {
        // --- Handle BevoegdGezagDTO and SoortRegelingDTO (similar logic as before) ---
        if (regelingDTO.getBevoegdGezag() != null) {
            Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO = bevoegdGezagRepository.findByCode(regelingDTO.getBevoegdGezag().getCode());
            BevoegdGezagDTO managedBevoegdGezagDTO;
            if (optionalBevoegdGezagDTO.isEmpty()) {
                managedBevoegdGezagDTO = bevoegdGezagRepository.save(regelingDTO.getBevoegdGezag());
            } else {
                managedBevoegdGezagDTO = optionalBevoegdGezagDTO.get();
            }
            regelingDTO.setBevoegdGezag(managedBevoegdGezagDTO);
        }

        if (regelingDTO.getType() != null) {
            Optional<SoortRegelingDTO> optionalSoortRegelingDTO = soortRegelingRepository.findByCode(regelingDTO.getType().getCode());
            SoortRegelingDTO managedSoortRegelingDTO;
            if (optionalSoortRegelingDTO.isEmpty()) {
                managedSoortRegelingDTO = soortRegelingRepository.save(regelingDTO.getType());
            } else {
                managedSoortRegelingDTO = optionalSoortRegelingDTO.get();
            }
            regelingDTO.setType(managedSoortRegelingDTO);
        }
    }

}