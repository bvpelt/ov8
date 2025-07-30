package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonGeoDownloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
