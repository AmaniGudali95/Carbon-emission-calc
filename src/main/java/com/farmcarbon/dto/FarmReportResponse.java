package com.farmcarbon.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record FarmReportResponse(Long farmId, String farmName, BigDecimal totalCo2eKg,
                                 BigDecimal co2eKgPerHectare) {
    public static FarmReportResponse of(Long farmId,
                                        String farmName,
                                        BigDecimal totalCo2eKg,
                                        BigDecimal sizeHectares) {
        BigDecimal perHectare = sizeHectares.compareTo(BigDecimal.ZERO)>0
                ? totalCo2eKg.divide(sizeHectares,2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return new FarmReportResponse(farmId,farmName,totalCo2eKg,perHectare);
    }

}
