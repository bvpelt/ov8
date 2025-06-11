package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonRegelingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Regeling;
import nl.overheid.omgevingswet.ozon.model.RegelingenSort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class RegelingenController {
    private static final Boolean goOn = true;
    private final OzonRegelingService ozonRegelingService;

    @GetMapping("/start")
    public ResponseEntity<List<Regeling>> triggerExternalProductFetch(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        log.info("Using page: {} size: {}", page, size);

        List<RegelingenSort> sort = List.of(RegelingenSort.REGISTRATIETIJDSTIP);

        ozonRegelingService.processEachRegeling(page, size, sort);

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/proces")
    public ResponseEntity<List<Regeling>> triggerExternalProductFetch() {
        int page = 1;
        int size = 10;
        boolean goOn = true;

        List<RegelingenSort> sort = List.of(RegelingenSort.REGISTRATIETIJDSTIP);

        while (goOn) {
            log.info("Using page: {} size: {}", page, size);
            try {
                ozonRegelingService.processEachRegeling(page, size, sort);
            } catch (Exception e) {
                log.error("Error processing regelingen", e);
                goOn = false;
            }
            if (goOn) {
                page++;
            }
        }
        return ResponseEntity.ok(List.of());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Regeling> getRegelingenStream(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        LocalDate geldigOp = LocalDate.now();
        LocalDate inWerkingOp = geldigOp;
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);
        String synchroniseerMetTileset = null;
        Boolean expand = null;
        List<RegelingenSort> sort = new ArrayList<>();
        sort.add(RegelingenSort.REGISTRATIETIJDSTIP);

        return ozonRegelingService.getRegelingenStream(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort)
                .doOnComplete(() -> log.info("Using stream page: {} size: {} regelingen", page, size));

    }

    @GetMapping(value = "/streamsave", produces = MediaType.ALL_VALUE)
    public Mono<String> getRegelingenStreamSave(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        LocalDate geldigOp = LocalDate.now();
        LocalDate inWerkingOp = geldigOp;
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);
        String synchroniseerMetTileset = null;
        Boolean expand = null;
        List<RegelingenSort> sort = new ArrayList<>();
        sort.add(RegelingenSort.REGISTRATIETIJDSTIP);

        return ozonRegelingService.procesRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, size, sort)
                .doOnSuccess(s -> log.info("Controller received success message: {}", s)) // Log the actual message
                .doOnError(e -> log.error("Controller received error: {}", e.getMessage())); // Add error logging here

    }

    @GetMapping(value = "/streamall", produces = MediaType.ALL_VALUE)
    public Mono<Void> getRegelingenStreamAll11(
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        Integer page = 1;
        Integer endpage = 5000;
        LocalDate geldigOp = LocalDate.now();
        LocalDate inWerkingOp = geldigOp;
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);
        String synchroniseerMetTileset = null;
        Boolean expand = null;
        List<RegelingenSort> sort = new ArrayList<>();
        sort.add(RegelingenSort.REGISTRATIETIJDSTIP);

        return ozonRegelingService.processAllRegelingen(geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, endpage, size, sort);
    }

    @GetMapping(value = "/streamall", produces = MediaType.TEXT_PLAIN_VALUE) // Explicitly produce text
    public Mono<String> getRegelingenStreamAll( // Change return type to Mono<String>
                                                @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        Integer page = 1;
        // Be careful with a very large endpage (like 5000) for a full fetch if not dynamically handled.
        // It could lead to long processing times or memory issues if not properly streamed.
        Integer endpage = 10; // Start with a smaller number for testing (e.g., 10 pages)
        // In a real scenario, you'd probably fetch total pages first, then iterate.
        LocalDate geldigOp = LocalDate.now();
        LocalDate inWerkingOp = geldigOp;
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);
        String synchroniseerMetTileset = null;
        Boolean expand = null;
        List<RegelingenSort> sort = new ArrayList<>();
        sort.add(RegelingenSort.REGISTRATIETIJDSTIP);

        log.info("Controller: Initiating processAllRegelingen from page {} to {} with pagesize: {}", page, endpage, size);

        return ozonRegelingService.processAllRegelingen(
                        geldigOp, inWerkingOp, beschikbaarOp, synchroniseerMetTileset, expand, page, endpage, size, sort
                )
                // Convert the Mono<Void> from the service into a Mono<String>
                .thenReturn("Regelingen processing initiated. Check server logs for progress.");
        // You can also add doOnSuccess/doOnError here for controller-level logging:
        // .doOnSuccess(voidResult -> log.info("Controller: processAllRegelingen completed successfully."))
        // .doOnError(e -> log.error("Controller: processAllRegelingen encountered an error: {}", e.getMessage(), e));
    }
}
