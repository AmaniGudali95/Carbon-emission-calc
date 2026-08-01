package com.farmcarbon.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmissionFactorCalculator {

    private static final BigDecimal FERTILIZER_N2O_FACTOR_PER_KG = BigDecimal.valueOf(4.42);
    private static final BigDecimal LIVESTOCK_FACTOR_PER_HEAD_YEAR = BigDecimal.valueOf(1800);
    private static final BigDecimal TILLAGE_FACTOR_PER_HECTARE = BigDecimal.valueOf(-150);

    public BigDecimal fertilizerCo2eKg(BigDecimal quantityKg) {
        return quantityKg.multiply(FERTILIZER_N2O_FACTOR_PER_KG);
    }

    public BigDecimal livestockCo2eKg(BigDecimal headCount) {
        return headCount.multiply(LIVESTOCK_FACTOR_PER_HEAD_YEAR);
    }

    public BigDecimal tillageCo2eKg(BigDecimal hectares) {
        return hectares.multiply(TILLAGE_FACTOR_PER_HECTARE);
    }
}
