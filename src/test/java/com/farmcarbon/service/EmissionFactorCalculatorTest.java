package com.farmcarbon.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class EmissionFactorCalculatorTest {
    private final EmissionFactorCalculator calculator = new EmissionFactorCalculator();

    @Test
    void calculatesFertilizerEmissions() {
        BigDecimal result = calculator.fertilizerCo2eKg(BigDecimal.valueOf(100));
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(442));
    }

    @Test
    void calculateLivestockEmissions() {
        BigDecimal result = calculator.livestockCo2eKg(BigDecimal.valueOf(5));
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(9000));
    }

    @Test
    void calculatesTillageEmissionsAsNegative() {
        BigDecimal result = calculator.tillageCo2eKg(BigDecimal.valueOf(12.5));
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(-1875));
        assertThat(result).isNegative();
    }

    @Test
    void zeroQuantityProducesZeroEmissions() {
        assertThat(calculator.fertilizerCo2eKg(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
