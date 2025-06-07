package com.bsoft.ov8.loader.services;


import com.bsoft.ov8.loader.clients.OzonRegelingenClient;
import lombok.extern.slf4j.Slf4j;
import nl.overheid.omgevingswet.ozon.model.Regelingen;
import nl.overheid.omgevingswet.ozon.model.RegelingenSort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class OzonRegelingService {
    private final OzonRegelingenClient ozonRegelingenClient;

    public OzonRegelingService(OzonRegelingenClient ozonRegelingenClient) {
        this.ozonRegelingenClient = ozonRegelingenClient;
    }

    public Mono<ResponseEntity<Regelingen>> getAllOzonRegelingen() {
        return ozonRegelingenClient._getRegelingen(null, null, null, null, null, 1, 10, null, null);
    }

    public void processEachRegeling(Integer page, Integer size, List<RegelingenSort> sort) {
        ozonRegelingenClient.processEachRegeling(null, null, null, null, null, page, size, sort);
    }
}
