package com.bsoft.ov8.loader.controller;

import com.bsoft.ov8.loader.services.OzonRegelingHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor // Lombok for constructor injection
@Slf4j
public class RegelingHistoryController {

    private final OzonRegelingHistoryService ozonRegelingHistoryService;

    @GetMapping("/proces")
    public void getRegelingen() {
        ozonRegelingHistoryService.processAll();
    }
}
