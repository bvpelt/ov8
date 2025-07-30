package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.GeometryDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.GeometryRepository;
import com.bsoft.ov8.loader.repositories.LocatieRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.geodownload.model.GeoJsonGeometry;
import nl.overheid.omgevingswet.ozon.presenteren.model.Regeling;
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
public class OzonGeoDownloadService {

    private final WebClient webClient;
    private final LocatieRepository locatieRepository;
    private final GeometryRepository geometryRepository;

    @Value("${api.ozon.download.base-url.base-url}")
    private String ozonBaseUrl;

    @Value("${api.ozon.download.epsg28992}")
    private String epsg28992;

    public OzonGeoDownloadService(WebClient webClient,
                                  LocatieRepository locatieRepository,
                                  GeometryRepository geometryRepository) {
        this.webClient = webClient;
        this.locatieRepository = locatieRepository;
        this.geometryRepository = geometryRepository;
    }

    /**
     * Process all regelingen with version > 1 sequentially
     */
    public void processAll() {
        final long start = System.currentTimeMillis();
        retrieveAndSaveGeometrien()
                .blockLast(); // Block to ensure completion

        log.info("Duration: {} ", (System.currentTimeMillis() - start));
    }

    /**
     * Retrieves RegelingDTOs from the database, fetches corresponding historical versions
     * from an external API, converts them, and saves them to the database SEQUENTIALLY.
     *
     * @return A Flux of RegelingDTOs that were successfully fetched and saved.
     */
    public Flux<RegelingDTO> retrieveAndSaveGeometrien() {
        return Mono.fromCallable(() -> locatieRepository.findNewGeometry())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                // KEY CHANGE: Use concatMap instead of flatMap for sequential processing
                .concatMap(this::processGeometrieIdentificatieSequentially)
                .onErrorContinue((throwable, obj) -> {
                    log.error("Error processing regeling: {}, error: {}", obj, throwable.getMessage());
                });
    }

    /**
     * Process a single regeling and all its historical versions sequentially
     */
    private Flux<RegelingDTO> processGeometrieIdentificatieSequentially(String geometrieIdentificatie) {
        log.info("0001 - Processing geometrie: {}", geometrieIdentificatie);

        // Process all historical versions for this regeling sequentially
        return processHistoricalVersionsSequentially(
                geometrieIdentificatie
        );
    }

    /**
     * Recursively fetch and save all historical versions sequentially
     */
    private Flux<RegelingDTO> processHistoricalVersionsSequentially(
            String geometrieIdentificatie) {

        log.info("0003 - processHistoricalVersionsSequentially geometrieIdentificatie: {}", geometrieIdentificatie);

        return fetchGeometrieFromApi(geometrieIdentificatie)
                .doOnSuccess(geoJsonGeometry -> {
                    //
                    // Convert GeoJsonGeometry to Geometry
                    //

                    //
                    // Save Geometry
                    //
                    GeometryDTO geometryDTO = new GeometryDTO();
                    geometryDTO.setGeoid(geometrieIdentificatie);

                })


                .onErrorResume(e -> {
                    log.error("0007 Error processing geometryIdentification {}, error: {}",
                            geometrieIdentificatie, e.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Makes a reactive API call to retrieve a Geometrie from the external download service.
     */
    private Mono<GeoJsonGeometry> fetchGeometrieFromApi(String geometrieIdentificatie) {

        log.info("0005 - fetchGeometrieFromApi geometrieIdentificatie: {}, crs: {}", geometrieIdentificatie, epsg28992);

        String apiPath = String.format("/geometrieen/%s", geometrieIdentificatie);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(ozonBaseUrl)
                .path(apiPath)
                .queryParam("crs", epsg28992);

        String uri = uriBuilder.build().toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GeoJsonGeometry.class)
                .doOnError(e -> log.error("API call error for {}: {}", geometrieIdentificatie, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}