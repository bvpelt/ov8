package com.bsoft.ov8.loader.services;

import com.bsoft.ov8.generated.api.RegelingenApi;
import com.bsoft.ov8.generated.model.Regeling;
import com.bsoft.ov8.generated.model.RegelingenSort;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.openapitools.jackson.nullable.JsonNullable.of;

@Service
public class OzonRegelingService {
    private final RegelingenApi regelingenApi;

    @Autowired
    public OzonRegelingService(RegelingenApi regelingenApi) {
        this.regelingenApi = regelingenApi;
    }

    public List<Regeling> getAllRegelingen() {
        try {
            int page = 1;
            int size = 200;
            String[] sort = {"registratietijdstip"};

            // The generated interface methods return ResponseEntity<T> by default
            ResponseEntity<List<Regeling>> response = regelingenApi._getRegelingen(null,null,null,null,false, page, size, sort  );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                // Handle non-2xx responses
                System.err.println("Failed to retrieve regelingen: " + response.getStatusCode());
                return List.of(); // Or throw a custom exception
            }
        } catch (FeignException e) {
            // FeignException wraps HTTP errors
            System.err.println("Error calling Ozon API: " + e.status() + " - " + e.getMessage());
            // You might want to wrap this in a custom business exception
            throw new RuntimeException("Failed to connect to Ozon service", e);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            throw new RuntimeException("Unexpected error during Ozon API call", e);
        }
    }

    public RegelingDto getRegelingById(Long id) {
        try {
            ResponseEntity<RegelingDto> response = regelingenApi.getRegelingById(id);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else if (response.getStatusCode().is4xxClientError()) {
                System.err.println("Regeling not found with ID: " + id);
                return null; // Or throw a specific NotFoundException
            } else {
                System.err.println("Failed to retrieve regeling by ID: " + response.getStatusCode());
                return null;
            }
        } catch (FeignException e) {
            System.err.println("Error calling Ozon API for ID " + id + ": " + e.status() + " - " + e.getMessage());
            throw new RuntimeException("Failed to get Regeling by ID " + id, e);
        }
    }
}
