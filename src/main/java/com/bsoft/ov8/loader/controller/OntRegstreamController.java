package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonOntwerpRegelingenStreamService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Ontwerpregeling;
import nl.overheid.omgevingswet.ozon.model.OntwerpregelingenSort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/stream")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class OntRegstreamController {

    private final OzonOntwerpRegelingenStreamService ozonOntwerpRegelingenStreamService;

    @GetMapping("/procesontwerp")
    Flux<Ontwerpregeling> getOntwerpRegelingen(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        List<OntwerpregelingenSort> sort = List.of(OntwerpregelingenSort.REGISTRATIETIJDSTIP);
        LocalDate geldigOp = LocalDate.now();
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);


        return ozonOntwerpRegelingenStreamService.getAllOntwerpRegelingen(
                beschikbaarOp,
                true,
                null,
                null,
                sort,
                null);
    }

    @GetMapping("/saveontwerp")
    public void saveOntwerpRegelingen(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Min(1) @Max(200) @Parameter(name = "size", description = "De pagesize moet minimaal een waarde van 1 hebben en maximaal een waarde van 200.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "size", required = false, defaultValue = "20") Integer size
    ) {
        List<OntwerpregelingenSort> sort = List.of(OntwerpregelingenSort.REGISTRATIETIJDSTIP);
        OffsetDateTime beschikbaarOp = OffsetDateTime.now(ZoneOffset.UTC);

        long start = System.currentTimeMillis();

        ozonOntwerpRegelingenStreamService.procesAll(
                beschikbaarOp,
                true,
                null,
                null,
                sort,
                null
        );
        long end = System.currentTimeMillis();

        log.info("Processing time: {} ms", end - start);
    }
}