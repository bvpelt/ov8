package com.bsoft.ov8.loader.clients;

import com.bsoft.ov8.loader.config.FeignClientConfig;
import com.bsoft.ov8.loader.ozonclient.api.RegelingenApi;
import org.springframework.cloud.openfeign.FeignClient;

// Define the Feign client
@FeignClient(
        name = "ozonRegelingenClient", // A unique name for your Feign client
        url = "${feign.client.config.ozonRegelingenClient.url}", // Points to configuration
        configuration = FeignClientConfig.class
)
// Or if you prefer to hardcode the URL (less flexible for environments):
// @FeignClient(name = "ozonRegelingenClient", url = "http://localhost:8080")
public interface OzonRegelingenClient extends RegelingenApi {
    // No methods needed here, as it inherits all methods from RegelingenApi
    // You can add custom methods if you need to extend functionality beyond the spec
}