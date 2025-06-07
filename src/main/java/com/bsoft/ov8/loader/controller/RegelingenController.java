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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class RegelingenController {
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
        int size = 200;
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
}
