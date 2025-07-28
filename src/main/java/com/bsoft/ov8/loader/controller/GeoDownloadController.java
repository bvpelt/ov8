package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonGeoDownloadService;
import com.bsoft.ov8.loader.services.OzonOntwerpRegelingenStreamService;
import com.bsoft.ov8.loader.services.OzonRegelingHistoryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.presenteren.model.Ontwerpregeling;
import nl.overheid.omgevingswet.ozon.presenteren.model.OntwerpregelingenSort;
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
@RequestMapping("/geo")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class GeoDownloadController {

    private final OzonGeoDownloadService ozonGeoDownloadService;

    @GetMapping("/download")
    public void getGeometries() {
        ozonGeoDownloadService.processAll();
    }
}
