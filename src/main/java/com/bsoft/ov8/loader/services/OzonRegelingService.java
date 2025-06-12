package com.bsoft.ov8.loader.services;


import com.bsoft.ov8.loader.clients.OzonRegelingenClient;
import com.bsoft.ov8.loader.database.BevoegdGezagDTO;
import com.bsoft.ov8.loader.database.RegelingDTO;
import com.bsoft.ov8.loader.database.SoortRegelingDTO;
import com.bsoft.ov8.loader.mappers.RegelingMapper;
import com.bsoft.ov8.loader.repositories.BevoegdGezagRepository;
import com.bsoft.ov8.loader.repositories.RegelingRepository;
import com.bsoft.ov8.loader.repositories.SoortRegelingRepository;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import nl.overheid.omgevingswet.ozon.model.Regelingen;
import nl.overheid.omgevingswet.ozon.model.RegelingenSort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class OzonRegelingService {
    private final OzonRegelingenClient ozonRegelingenClient;

    private final RegelingMapper regelingMapper;

    private final BevoegdGezagRepository bevoegdGezagRepository;

    private final RegelingRepository regelingRepository;

    private final SoortRegelingRepository soortRegelingRepository;

    public OzonRegelingService(OzonRegelingenClient ozonRegelingenClient,
                               RegelingMapper regelingMapper,
                               BevoegdGezagRepository bevoegdGezagRepository,
                               RegelingRepository regelingRepository,
                               SoortRegelingRepository soortRegelingRepository
    ) {
        this.ozonRegelingenClient = ozonRegelingenClient;
        this.regelingMapper = regelingMapper;
        this.bevoegdGezagRepository = bevoegdGezagRepository;
        this.regelingRepository = regelingRepository;
        this.soortRegelingRepository = soortRegelingRepository;
    }

    public Mono<ResponseEntity<Regelingen>> getAllOzonRegelingen() {
        return ozonRegelingenClient._getRegelingen(null, null, null, null, null, 1, 10, null, null);
    }

    public void processEachRegeling(Integer page, Integer size, List<RegelingenSort> sort) {
        // IMPORTANT: You MUST subscribe to the Mono to activate the reactive stream.
        ozonRegelingenClient.processAllRegelingen(null, null, null, null, null, page, page, size, sort, null)
                .subscribe(
                        // onComplete handler (when the Mono<Void> finishes successfully)
                        null, // You can pass a Runnable or Consumer<Void> here if you want
                        // to do something on success, but for Mono<Void>, often null is fine if no action
                        // onError handler
                        throwable -> log.error("Error during processing all regelingen: {}", throwable.getMessage(), throwable),
                        // onComplete handler
                        () -> log.info("Finished processing all regelingen for page {} and size {}", page, size)
                );

        // Alternatively, if you want this method to also return a Mono<Void>
        // so the caller can subscribe and know when it's done:
        // public Mono<Void> processEachRegeling(Integer page, Integer size, List<RegelingenSort> sort) {
        //     return ozonRegelingenClient.processAllRegelingen(null, null, null, null, null, page, page, size, sort, null);
        // }
        // In that case, the subscriber would be in the controller or wherever this service method is called.
    }

    public Flux<Regeling> getRegelingenStream(LocalDate geldigOp,
                                              LocalDate inWerkingOp,
                                              OffsetDateTime beschikbaarOp,
                                              String synchroniseerMetTileset,
                                              Boolean expand,
                                              Integer page,
                                              Integer size,
                                              List<RegelingenSort> sort) {

        Flux<Regeling> regelingFlux = ozonRegelingenClient.getIndividualRegelingenFlux(
                geldigOp,
                inWerkingOp,
                beschikbaarOp,
                synchroniseerMetTileset,
                expand,
                page,
                size,
                sort,
                null);

        //log.info("Aantal elementen: {}", regelingFlux.count());

        return regelingFlux;
    }

    public Mono<String> procesRegelingen(LocalDate geldigOp,
                                         LocalDate inWerkingOp,
                                         OffsetDateTime beschikbaarOp,
                                         String synchroniseerMetTileset,
                                         Boolean expand,
                                         Integer page,
                                         Integer size,
                                         List<RegelingenSort> sort
    ) {
        // The entire chain that starts with getRegelingenStream is a single reactive pipeline
        // that eventually results in a Mono<String>. You need to return THAT Mono<String>.
        return getRegelingenStream(geldigOp,
                inWerkingOp,
                beschikbaarOp,
                synchroniseerMetTileset,
                expand,
                page,
                size,
                sort)
                .doOnNext(regeling -> {
                    log.info("Processing Regeling with ID: {}", regeling.getIdentificatie());
                    log.info("Processing individual Regeling id: {}", regeling.getIdentificatie());
                    log.debug("regeling: {}", regeling);
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    log.debug("regelingDTO: {}", regelingDTO.toString());
                    saveRegeling(regelingDTO);
                })
                .count() // Counts the number of processed regelingen
                .map(count -> {
                    // This map runs after the flux completes and emits the count
                    String message = String.format("Successfully processed %d regelingen.", count);
                    log.info(message);
                    return message;
                })
                .onErrorResume(throwable -> {
                    // Handle errors gracefully and return an error message
                    log.error("An error occurred during processing regelingen: {}", throwable.getMessage(), throwable);
                    return Mono.just("Error during processing: " + throwable.getMessage());
                });
        // The commented line was an immediate return of a hardcoded value, which is not what you want
        // as it would return before the async processing starts.
        // The reactive chain itself IS the return value.
    }

    //@Transactional
    public void saveRegeling(RegelingDTO regelingDTO) {

        String bevoegdGezagCode;
        Optional<BevoegdGezagDTO> optionalBevoegdGezagDTO;
        BevoegdGezagDTO savedBevoegdGezagDTO = null;

        Optional<RegelingDTO> optionalRegelingDTO = regelingRepository.findByIdentificatieAndTijdstipregistratieAndBegingeldigheid(
                regelingDTO.getIdentificatie(),
                regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                regelingDTO.getRegistratiegegevens().getBeginGeldigheid());

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
            log.info("Regeling identificatie {} tijdstipRegistratie: {}, beginGeldigheid: {} exists",
                    regelingDTO.getIdentificatie(),
                    regelingDTO.getRegistratiegegevens().getTijdstipRegistratie(),
                    regelingDTO.getRegistratiegegevens().getBeginGeldigheid());
        }
    }

    // This is the new helper method: processRegelingenForPage
    // It encapsulates the logic from your previous 'procesRegelingen' method
    // and returns Mono<Void> because its primary purpose in the larger chain
    // is to complete a side-effect (saving to DB) and signal completion, not return a string.
    public Mono<Void> processRegelingenForPage(LocalDate geldigOp,
                                               LocalDate inWerkingOp,
                                               OffsetDateTime beschikbaarOp,
                                               String synchroniseerMetTileset,
                                               Boolean expand,
                                               Integer page,
                                               Integer size,
                                               List<RegelingenSort> sort) {

        return getRegelingenStream(geldigOp,
                inWerkingOp,
                beschikbaarOp,
                synchroniseerMetTileset,
                expand,
                page,
                size,
                sort)
                .doOnNext(regeling -> {
                    log.info("Processing Regeling from page {} with ID: {}", page, regeling.getIdentificatie());
                    log.debug("regeling: {}", regeling);
                    RegelingDTO regelingDTO = regelingMapper.toRegelingDTO(regeling);
                    log.debug("regelingDTO: {}", regelingDTO.toString());
                    saveRegeling(regelingDTO); // Call your synchronous save method
                })
                .onErrorResume(throwable -> {
                    // Log the error for this specific page but allow the overall Flux to continue
                    log.error("An error occurred during processing regelingen for page {}: {}", page, throwable.getMessage(), throwable);
                    return Mono.empty(); // Continue to the next page, or propagate error if critical
                })
                .then(); // Convert Flux<Regeling> to Mono<Void> when all elements are processed
    }

    public Mono<Void> processAllRegelingen(
            LocalDate geldigOp,
            LocalDate inWerkingOp,
            OffsetDateTime beschikbaarOp,
            String synchroniseerMetTileset,
            Boolean expand,
            int startPage,
            int endPage,
            int pageSize,
            List<RegelingenSort> sort
    ) {
        return Flux.range(startPage, endPage - startPage + 1) // Flux of page numbers
                .flatMap(page -> processRegelingenForPage( // Process each page concurrently
                        geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset,
                        expand, page, pageSize, sort
                ), 5) // Limit concurrency
                // Apply doOnError and onErrorResume *before* .then(), on the Flux<Void> from flatMap
                .doOnError(e -> log.error("An error occurred in a page processing stream: {}", e.getMessage(), e)) // This doOnError applies to errors from individual pages
                .onErrorResume(e -> {
                    log.error("Failed to fetch or process regelingen from Ozon API for a page: {}", e.getMessage(), e);
                    // This onErrorResume applies to errors from individual pages.
                    // Returning Mono.empty() here means if one page fails, the overall Flux will continue.
                    // If you want the *entire* process to stop if *any* page fails, return Mono.error(e) instead.
                    return Mono.empty();
                })
                .then() // Convert Flux<Void> to Mono<Void>
                // Apply doOnSuccess and another doOnError *after* .then(), on the resulting Mono<Void>
                .doOnSuccess(voidResult -> log.info("Successfully completed processing all regelingen.")) // This doOnSuccess applies to the overall Mono<Void>
                .doOnError(e -> log.error("An unhandled error occurred during overall regelingen processing: {}", e.getMessage(), e)); // This doOnError applies to the overall Mono<Void>
    }
}
