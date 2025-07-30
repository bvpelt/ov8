package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.database.GeometryDTO;
import com.bsoft.ov8.loader.repositories.GeometryRepository;
import com.bsoft.ov8.loader.repositories.LocatieRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.geodownload.model.GeoJsonGeometry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class OzonGeoDownloadService {

    private final WebClient webClient;
    private final LocatieRepository locatieRepository;
    private final GeometryRepository geometryRepository;
    private final GeometryConverter geometryConverter;

    @Value("${api.ozon.download.base-url}")
    private String ozonBaseUrl;

    @Value("${api.ozon.download.epsg28992}")
    private String epsg28992;

    public OzonGeoDownloadService(WebClient webClient,
                                  LocatieRepository locatieRepository,
                                  GeometryRepository geometryRepository,
                                  GeometryConverter geometryConverter) {
        this.webClient = webClient;
        this.locatieRepository = locatieRepository;
        this.geometryRepository = geometryRepository;
        this.geometryConverter = geometryConverter;
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
    public Flux<GeometryDTO> retrieveAndSaveGeometrien() {
        return Mono.fromCallable(() -> locatieRepository.findNewGeometry())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                // Use concatMap for sequential processing
                .concatMap(this::processGeometrieIdentificatieSequentially)
                .onErrorContinue((throwable, obj) -> {
                    log.error("Error processing geometry identification: {}, error: {}", obj, throwable.getMessage());
                });
    }

    /**
     * Process a single regeling and all its historical versions sequentially
     */
    private Flux<GeometryDTO> processGeometrieIdentificatieSequentially(String geometrieIdentificatie) {
        log.info("Processing geometry: {}", geometrieIdentificatie);

        return processGeometrySequentially(geometrieIdentificatie);
    }

    /**
     * Recursively fetch and save all historical versions sequentially
     */
    private Flux<GeometryDTO> processGeometrySequentially(String geometrieIdentificatie) {
        log.info("Processing geometry identification: {}", geometrieIdentificatie);

        return fetchGeometrieFromApi(geometrieIdentificatie)
                .flatMap(geoJsonGeometry -> {
                    // Convert GeoJsonGeometry to JTS Geometry
                    try {
                        org.locationtech.jts.geom.Geometry jtsGeometry =
                                geometryConverter.convertGeoJsonToJtsGeometry(geoJsonGeometry.toString());

                        if (jtsGeometry == null) {
                            log.warn("Failed to convert GeoJSON to JTS Geometry for: {}", geometrieIdentificatie);
                            return Mono.empty();
                        }

                        // Create and save GeometryDTO
                        GeometryDTO geometryDTO = new GeometryDTO();
                        geometryDTO.setGeoid(geometrieIdentificatie);
                        geometryDTO.setGeometrie(jtsGeometry);

                        // Save to database using reactive repository or blocking operation
                        return saveGeometryDTO(geometryDTO);

                    } catch (Exception e) {
                        log.error("Error converting geometry for {}: {}", geometrieIdentificatie, e.getMessage());
                        return Mono.empty();
                    }
                })
                .flux() // Convert Mono to Flux
                .onErrorResume(e -> {
                    log.error("Error processing geometry identification {}: {}",
                            geometrieIdentificatie, e.getMessage());
                    return Flux.empty();
                });
    }

    /**
     * Save GeometryDTO to database
     * If your repository is reactive, use it directly. Otherwise, wrap in Mono.fromCallable
     */
    private Mono<GeometryDTO> saveGeometryDTO(GeometryDTO geometryDTO) {
        return Mono.fromCallable(() -> {
                    GeometryDTO saved = geometryRepository.save(geometryDTO);
                    log.info("Successfully saved geometry with ID: {} for geoid: {}",
                            saved.getId(), saved.getGeoid());
                    return saved;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    log.error("Error saving geometry for geoid {}: {}", geometryDTO.getGeoid(), e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Makes a reactive API call to retrieve a Geometrie from the external download service.
     */
    private Mono<GeoJsonGeometry> fetchGeometrieFromApi(String geometrieIdentificatie) {
        log.info("Fetching geometry from API - ID: {}, CRS: {}", geometrieIdentificatie, epsg28992);

        String apiPath = String.format("/geometrieen/%s", geometrieIdentificatie);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(ozonBaseUrl)
                .path(apiPath)
                .queryParam("crs", epsg28992);

        String uri = uriBuilder.build().toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GeoJsonGeometry.class)
                .doOnSuccess(geometry -> log.debug("Successfully fetched geometry for: {}", geometrieIdentificatie))
                .doOnError(e -> log.error("API call error for {}: {}", geometrieIdentificatie, e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Failed to fetch geometry for {}: {}", geometrieIdentificatie, e.getMessage());
                    return Mono.empty();
                });
    }
}