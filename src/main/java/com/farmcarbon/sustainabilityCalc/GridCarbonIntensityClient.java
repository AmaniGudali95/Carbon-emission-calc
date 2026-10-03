package com.farmcarbon.sustainabilityCalc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GridCarbonIntensityClient {
    private final RestClient restClient;
    private final String zone;

    public GridCarbonIntensityClient(
            @Value("${electricity-maps.base-url}") String baseUrl,
            @Value("${electricity-maps.api-key}") String apiKey,
            @Value("${electricity-maps.zone}") String zone
    ){
        this.restClient= RestClient.builder().baseUrl(baseUrl)
                .defaultHeader("auth-token", apiKey).build();
        this.zone=zone;
    }

    @Cacheable(value = "gridIntensity")
    public double getCurrentCarbonIntensityGramsPerKwh() {
        CarbonIntensityResponse response = restClient.get()
                .uri("/carbon-intensity/latest?zone={zone}", zone)
                .retrieve()
                .body(CarbonIntensityResponse.class);
        return response.carbonIntensity();

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CarbonIntensityResponse(double carbonIntensity) {

    }
}
