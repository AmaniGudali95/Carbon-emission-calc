package com.farmcarbon.service;


import com.farmcarbon.client.CarbonInterfaceClient;
import com.farmcarbon.entity.Activity;
import com.farmcarbon.entity.EmissionRecord;
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
        switch (activity.getActivityType()) {
            case FUEL_COMBUSTION -> {
                co2eKg=carbonInterfaceClient.estimateFuelCombustionCo2eKg(activity.getQuantity().doubleValue());
                source = "Carbon Interface";
            }
            case IRRIGATION_ENERGY -> {
                co2eKg = carbonInterfaceClient.estimateElectricityCo2eKg(quantity.doubleValue());
                source = "Carbon Interface";
            }
            case FERTILIZER_APPLICATION -> {
                co2eKg = emissionFactorCalculator.fertilizerCo2eKg(quantity);
                source ="IPCC default factor";
            }
            case LIVESTOCK -> {
                co2eKg = emissionFactorCalculator.livestockCo2eKg(quantity);
                source="IPCC default factor";
            }
            case TILLAGE -> {
                co2eKg = emissionFactorCalculator.tillageCo2eKg(quantity);
                source="IPCC default factor";
            }
            default -> throw new UnsupportedOperationException("Emissions calculation not yet implemented for" + activity.getActivityType());
        }

        EmissionRecord record = new EmissionRecord(activity, co2eKg, source);
        EmissionRecord saved = emissionRecordRepository.save(record);

        log.info("Saved emission record id={} co2eKg={} source={} for activity id={}",
                saved.getId(), saved.getCo2eKg(), saved.getEmissionFactorSource(), activity.getId());
        return saved;
    }
}
