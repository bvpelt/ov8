package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.ozonclient.model.Regeling;
import com.bsoft.ov8.loader.services.OzonRegelingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class RegelingenController {
    private final OzonRegelingService ozonRegelingService;

    @GetMapping("/regelingen")
    public ResponseEntity<List<Regeling>> triggerExternalProductFetch(
            @Min(1) @Parameter(name = "page", description = "De page moet minimaal een waarde van 1 hebben.", in = ParameterIn.QUERY) @Valid @RequestParam(value = "page", required = false, defaultValue = "1") Integer page
    ) {
        log.info("Using page: {}", page);
        return ResponseEntity.ok(ozonRegelingService.getAllRegelingen(page));
    }
}
