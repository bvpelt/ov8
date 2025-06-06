package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.loader.ozonclient.api.RegelingenApi;
import com.bsoft.ov8.loader.ozonclient.model.Regeling;
import com.bsoft.ov8.loader.ozonclient.model.Regelingen;
import com.bsoft.ov8.loader.ozonclient.model.RegelingenSort;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OzonRegelingService {
    private final RegelingenApi regelingenApi;

    @Autowired
    public OzonRegelingService(RegelingenApi regelingenApi) {
        this.regelingenApi = regelingenApi;
    }

    public List<Regeling> getAllRegelingen(Integer page ) {
        try {

            int size = 200;
            List<RegelingenSort> sort = List.of(RegelingenSort._REGISTRATIETIJDSTIP);

            // The generated interface methods return ResponseEntity<T> by default
            ResponseEntity<Regelingen> response = regelingenApi._getRegelingen(null, null, null, null, false, page, size, sort);
            if (response.getStatusCode().is2xxSuccessful()) {
                if (response.getBody() != null) {
                    return response.getBody().getEmbedded().getRegelingen();
                } else {
                    return List.of();
                }
            } else {
                // Handle non-2xx responses
                log.error("Failed to retrieve regelingen: {} ", response.getStatusCode());
                return List.of(); // Or throw a custom exception
            }
        } catch (FeignException e) {
            // FeignException wraps HTTP errors
            log.error("Error calling Ozon API: {} - {}", e.status(), e.getMessage());
            // You might want to wrap this in a custom business exception
            throw new RuntimeException("Failed to connect to Ozon service", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            throw new RuntimeException("Unexpected error during Ozon API call", e);
        }
    }

    public Regeling getRegelingById(String id) {
        try {
            ResponseEntity<Regeling> response = regelingenApi._getRegeling(id, null, null, null, null, null);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else if (response.getStatusCode().is4xxClientError()) {
                log.error("Regeling not found with ID: " + id);
                return null; // Or throw a specific NotFoundException
            } else {
                log.error("Failed to retrieve regeling by ID: " + response.getStatusCode());
                return null;
            }
        } catch (FeignException e) {
            log.error("Error calling Ozon API for ID " + id + ": " + e.status() + " - " + e.getMessage());
            throw new RuntimeException("Failed to get Regeling by ID " + id, e);
        }
    }
}
