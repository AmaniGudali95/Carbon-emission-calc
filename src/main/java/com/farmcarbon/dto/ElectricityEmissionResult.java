package com.farmcarbon.dto;

import java.math.BigDecimal;

public record ElectricityEmissionResult(
        BigDecimal locationBasedCo2eKg,
        BigDecimal marketBasedCo2eKg,
        String marketBasedMethodUsed
) {
}
