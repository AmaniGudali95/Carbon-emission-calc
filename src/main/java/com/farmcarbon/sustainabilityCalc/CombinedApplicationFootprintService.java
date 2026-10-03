package com.farmcarbon.sustainabilityCalc;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CombinedApplicationFootprintService {
    private final ApplicationEnergyEstimator energyEstimator;
    private final FarmBeatsFootprintClient farmBeatsFootprintClient;
    private final GridCarbonIntensityClient gridCarbonIntensityClient;

    public CombinedApplicationFootprintService(
            ApplicationEnergyEstimator energyEstimator,
            FarmBeatsFootprintClient farmBeatsFootprintClient,
            GridCarbonIntensityClient gridCarbonIntensityClient
    ) {
        this.energyEstimator = energyEstimator;
        this.farmBeatsFootprintClient=farmBeatsFootprintClient;
        this.gridCarbonIntensityClient=gridCarbonIntensityClient;
    }

    public CombinedFootprint calculateCombinedFootprint() {
        double ownKwh = energyEstimator.estimateCumulativeEnergyKwh();
        double farmbeatsKwh = farmBeatsFootprintClient.getEstimatedEnergyKwh();
        double combinedKwh = ownKwh + farmbeatsKwh;

        double gridIntensityGramsPerKwh = gridCarbonIntensityClient.getCurrentCarbonIntensityGramsPerKwh();

        return new CombinedFootprint(
                toKg(ownKwh, gridIntensityGramsPerKwh),
                toKg(farmbeatsKwh, gridIntensityGramsPerKwh),
                toKg(combinedKwh, gridIntensityGramsPerKwh),
                BigDecimal.valueOf(gridIntensityGramsPerKwh).setScale(1, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal toKg(double kWh, double gramsPerKwh) {
        return BigDecimal.valueOf((kWh * gramsPerKwh)/1000.0).setScale(4, RoundingMode.HALF_UP);
    }

    public record CombinedFootprint(
            BigDecimal farmCarbonServiceCo2eKg,
            BigDecimal farmBeatsCo2eKg,
            BigDecimal totalCo2eKg,
            BigDecimal gridCarbonIntensityGramsPerKwh
    ){}
}
