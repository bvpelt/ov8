package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.clients.OzonRegelingenClient;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class OzonRegelingHistoryService {

    private final OzonRegelingenClient ozonRegelingenClient;
    private final WebClient webClient;
    private final RegelingRepository regelingRepository;

    public OzonRegelingHistoryService(OzonRegelingenClient ozonRegelingenClient,
                                      WebClient webClient,
                                      RegelingRepository regelingRepository) {
        this.ozonRegelingenClient = ozonRegelingenClient;
        this.webClient = webClient;
        this.regelingRepository = regelingRepository;
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

    }

    /**
     * Retrieves historical versions of RegelingDTOs by querying the database for
     * RegelingDTOs with versie > 1 and then making reactive API calls for previous versions.
     *
     * @return A Flux of RegelingDTOs representing the previous versions retrieved from the API.
     */
    public Flux<RegelingDTO> retrieveHistoricalRegelingenReactive() {
        // 1. Get RegelingDTOs from the database with versie > 1
        List<RegelingDTO> regelingenWithHistory = regelingRepository.findByVersieGreaterThan(1);

        // 2. Convert the list to a Flux
        return Flux.fromIterable(regelingenWithHistory)
                .flatMap(currentRegeling -> {
                    // Generate a stream of version numbers from (current version - 1) down to 1
                    List<Integer> versionsToFetch = IntStream.rangeClosed(1, currentRegeling.getRegistratiegegevens().getVersie().intValue() - 1)
                            .boxed()
                            .collect(Collectors.toList());

                    // For each version number, make an API call
                    return Flux.fromIterable(versionsToFetch)
                            .flatMap(versionToFetch ->
                                    fetchSpecificVersionFromApi(currentRegeling.getIdentificatie(), versionToFetch)
                            );
                }); // Make a reactive API call for each
    }

    /**
     * Makes a reactive API call to retrieve a specific version of a RegelingDTO.
     * Assumes an API endpoint like /api/regelingen/{identificatie}/versions?version={versionNumber}
     *
     * @param identificatie The unique identifier of the regeling.
     * @param versionToFetch The specific version number to retrieve.
     * @return A Mono that emits the specific RegelingDTO version, or an empty Mono if not found/error.
     */
    private Mono<RegelingDTO> fetchSpecificVersionFromApi(String identificatie, Integer versionToFetch) {
        // Construct the API URL. Adapt this to your actual API's structure.
        // This is a hypothetical example assuming querying by version number.
        String apiPath = String.format("/api/regelingen/%s/versions", identificatie);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(apiPath)
                        .queryParam("version", versionToFetch) // Query by version number
                        .build())
                .retrieve()
                .bodyToMono(RegelingDTO.class) // Assuming the API returns a single RegelingDTO
                .doOnError(e -> System.err.println("Error fetching version " + versionToFetch +
                        " for " + identificatie + ": " + e.getMessage()))
                .onErrorResume(e -> Mono.empty()); // Return empty Mono on error, or handle as needed
    }
}
