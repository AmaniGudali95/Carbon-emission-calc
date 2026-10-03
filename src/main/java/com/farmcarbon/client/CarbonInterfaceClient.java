package com.farmcarbon.client;


import com.farmcarbon.dto.external.CarbonInterfaceRequest;
import com.farmcarbon.dto.external.CarbonInterfaceResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class CarbonInterfaceClient {

    private final RestClient restClient;

    public CarbonInterfaceClient(
            @Value("${carbon-interface.base-url}") String baseUrl,
            @Value("${carbon-interface.api-key}") String apiKey
    )
    {
        this.restClient= RestClient.builder().baseUrl(baseUrl)
                .defaultHeader("Authorization","Bearer "+apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Cacheable(value = "fuelEstimates", key = "#litres")
    public BigDecimal estimateFuelCombustionCo2eKg(double liters) {
        CarbonInterfaceResponse response = restClient.post()
                .uri("/estimates")
                .body(CarbonInterfaceRequest.fuelCombustion(liters))
                .retrieve()
                .body(CarbonInterfaceResponse.class);
        return BigDecimal.valueOf(response.data().attributes().carbon_kg());
    }
    @Cacheable(value = "electricityEstimates", key = "#kWh")
    public BigDecimal estimateElectricityCo2eKg(double kWh) {
        CarbonInterfaceResponse response = restClient.post()
                .uri("/estimates")
                .body(CarbonInterfaceRequest.electricity(kWh))
                .retrieve()
                .body(CarbonInterfaceResponse.class);
        return BigDecimal.valueOf(response.data().attributes().carbon_kg());
    }
}
