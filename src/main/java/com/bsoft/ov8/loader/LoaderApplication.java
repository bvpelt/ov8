package com.bsoft.ov8.loader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
// Point to the package where OzonRegelingenFeignClient is located.
// If it's in com.bsoft.ov8.loader.client, then this is fine.
// You can also use a broader package like "com.bsoft.ov8" if all your clients are under it.
@EnableFeignClients(basePackages = "com.bsoft.ov8.loader.clients")
// If you want to keep the generated API package also scanned for other potential clients:
// @EnableFeignClients(basePackages = {"com.bsoft.ov8.loader.clients", "com.bsoft.ov8.ozonclient.api"})
public class LoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoaderApplication.class, args);
    }

}
