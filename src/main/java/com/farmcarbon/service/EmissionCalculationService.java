package com.farmcarbon.service;


import com.farmcarbon.client.CarbonInterfaceClient;
import com.farmcarbon.dto.ElectricityEmissionResult;
import com.farmcarbon.entity.Activity;
import com.farmcarbon.entity.DataQualityTier;
import com.farmcarbon.entity.EmissionRecord;
import com.farmcarbon.entity.GhgScope;
import com.farmcarbon.repository.EmissionRecordRepository;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class EmissionCalculationService {
    private static final Logger log = LoggerFactory.getLogger(EmissionCalculationService.class);
    private final CarbonInterfaceClient carbonInterfaceClient;
    private final EmissionRecordRepository emissionRecordRepository;

    private final EmissionFactorCalculator emissionFactorCalculator;

    public EmissionCalculationService(
            CarbonInterfaceClient carbonInterfaceClient,
            EmissionRecordRepository emissionRecordRepository,
            EmissionFactorCalculator emissionFactorCalculator
    ) {
        this.carbonInterfaceClient=carbonInterfaceClient;
        this.emissionRecordRepository=emissionRecordRepository;
        this.emissionFactorCalculator=emissionFactorCalculator;
    }

    public EmissionRecord calculateAndSave(Activity activity) {
        BigDecimal co2eKg;
        BigDecimal quantity = activity.getQuantity();
        String source;
        DataQualityTier tier;
        switch (activity.getActivityType()) {
            case FUEL_COMBUSTION -> {
                co2eKg=carbonInterfaceClient.estimateFuelCombustionCo2eKg(activity.getQuantity().doubleValue());
                source = "Carbon Interface";
                tier= DataQualityTier.AVERAGE_DATA;

            }
            case IRRIGATION_ENERGY -> {
                ElectricityEmissionResult electricityResult = calculateElectricityEmissions(
                        quantity.doubleValue(), activity.getFarm().getRenewableContractId()
                );
                co2eKg = electricityResult.locationBasedCo2eKg();
                // carbonInterfaceClient.estimateElectricityCo2eKg(quantity.doubleValue());
                source = "Carbon Interface";
                tier= DataQualityTier.AVERAGE_DATA;
            }
            case FERTILIZER_APPLICATION -> {
                co2eKg = emissionFactorCalculator.fertilizerCo2eKg(quantity);
                source ="IPCC default factor";
                tier= DataQualityTier.AVERAGE_DATA;
            }
            case LIVESTOCK -> {
                co2eKg = emissionFactorCalculator.livestockCo2eKg(quantity);
                source="IPCC default factor";
                tier= DataQualityTier.AVERAGE_DATA;
            }
            case TILLAGE -> {
                co2eKg = emissionFactorCalculator.tillageCo2eKg(quantity);
                source="IPCC default factor";
                tier= DataQualityTier.AVERAGE_DATA;
            }
            default -> throw new UnsupportedOperationException("Emissions calculation not yet implemented for" + activity.getActivityType());
        }
        GhgScope scope = activity.getActivityType().getGhgScope();

        EmissionRecord record = new EmissionRecord(activity, co2eKg, source, scope, tier);
        EmissionRecord saved = emissionRecordRepository.save(record);

        log.info("Saved emission record id={} co2eKg={} source={} for activity id={}",
                saved.getId(), saved.getCo2eKg(), saved.getEmissionFactorSource(), activity.getId());
        return saved;
    }

    private ElectricityEmissionResult calculateElectricityEmissions(double kWh, String renewableContractId) {
        BigDecimal locationBased = carbonInterfaceClient.estimateElectricityCo2eKg(kWh);

        if (renewableContractId == null) {
            return new ElectricityEmissionResult(locationBased, locationBased, "same as location based (no contract data)");
        }

        throw new UnsupportedOperationException(
                "Supplier-specific market-based method not yet implemented for contract: " + renewableContractId
        );
    }
}
