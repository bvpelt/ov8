package com.bsoft.ov8.loader.clients;

import com.bsoft.ov8.ozonclient.api.RegelingenApi;
import org.springframework.cloud.openfeign.FeignClient;

// Define the Feign client
@FeignClient(
        name = "ozonRegelingenService", // A unique name for your Feign client
        url = "${feign.client.config.ozonRegelingenService.url}" // Points to configuration
)
// Or if you prefer to hardcode the URL (less flexible for environments):
// @FeignClient(name = "ozonRegelingenService", url = "http://localhost:8080")
public interface OzonRegelingenFeignClient extends RegelingenApi {
    // No methods needed here, as it inherits all methods from RegelingenApi
    // You can add custom methods if you need to extend functionality beyond the spec
}