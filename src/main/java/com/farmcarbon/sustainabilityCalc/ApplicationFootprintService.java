package com.farmcarbon.sustainabilityCalc;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ApplicationFootprintService {
    private final ApplicationEnergyEstimator energyEstimator;
    private final GridCarbonIntensityClient gridCarbonIntensityClient;

    public ApplicationFootprintService(
            ApplicationEnergyEstimator energyEstimator,
            GridCarbonIntensityClient gridCarbonIntensityClient
    ){
        this.energyEstimator=energyEstimator;
        this.gridCarbonIntensityClient=gridCarbonIntensityClient;
    }

    public  ApplicationFootprint calculateCurrentFootprint() {
        double cumulativeKwh = energyEstimator.estimateCumulativeEnergyKwh();
        double gridIntensityGramsPerKwh = gridCarbonIntensityClient.getCurrentCarbonIntensityGramsPerKwh();

        double co2eGrams = cumulativeKwh * gridIntensityGramsPerKwh;
        BigDecimal co2eKg = BigDecimal.valueOf(co2eGrams / 1000.0).setScale(4, RoundingMode.HALF_UP);

        return new ApplicationFootprint(
                BigDecimal.valueOf(cumulativeKwh).setScale(4, RoundingMode.HALF_UP),
                BigDecimal.valueOf(gridIntensityGramsPerKwh).setScale(1, RoundingMode.HALF_UP),
                co2eKg
        );
    }

    public record ApplicationFootprint(
            BigDecimal estimatedEnergyKwh,
            BigDecimal gridCarbonIntensityGramsPerKwh,
            BigDecimal estimatedCo2eKg
    ) {}
}