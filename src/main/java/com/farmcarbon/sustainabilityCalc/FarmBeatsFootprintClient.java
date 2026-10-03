package com.farmcarbon.sustainabilityCalc;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class FarmBeatsFootprintClient {
    private static final Logger log = LoggerFactory.getLogger(FarmBeatsFootprintClient.class);
    private final RestClient restClient;
    
    public FarmBeatsFootprintClient(
            @Value("${farmbeats.base-url}") String baseUrl
    ) {
        this.restClient=RestClient.builder().baseUrl(baseUrl).build();
    }

    public double getEstimatedEnergyKwh() {
        try {
            EnergyResponse response = restClient.get()
                    .uri("/sustainability/energy")
                    .retrieve()
                    .body(EnergyResponse.class);
            return response!=null ? response.estimatedEnergyKwh() : 0.0;

        } catch (RestClientException e) {
            log.warn("Could not reach FarmBeats sustainability endpoint, excluding from combined footprint", e);
            return 0.0;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EnergyResponse(double estimatedEnergyKwh) {}
}
