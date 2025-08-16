package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonRegelingHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/save")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class RegelingHistoryController {

    private final OzonRegelingHistoryService ozonRegelingHistoryService;

    @GetMapping("/regelingenhistoriex")
    public void getRegelingenx() {
        long start = System.currentTimeMillis();

        ozonRegelingHistoryService.processAll();

        long end = System.currentTimeMillis();

        log.info("Processing time: {} ms", end - start);
    }

    @GetMapping(value = "/regelingenhistorie", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getRegelingen() {
        long start = System.currentTimeMillis();

        List<String> processedIdentifications = ozonRegelingHistoryService.processAllAndReturnIdentifications();

        long end = System.currentTimeMillis();

        log.info("Processing time: {} ms", end - start);
        log.info("Processed {} regelingen with historical data", processedIdentifications.size());

        return processedIdentifications;
    }

    // Just return what's already stored (no processing)
    @GetMapping(value = "/histcand", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getStoredRegelingen() {
        return ozonRegelingHistoryService.getStoredHistoricalRegelingenIdentifications();
    }
}
