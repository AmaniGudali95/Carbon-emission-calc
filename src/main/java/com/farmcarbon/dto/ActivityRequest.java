package com.farmcarbon.dto;

import com.farmcarbon.entity.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityRequest (
    @NotNull ActivityType activityType,
    @NotNull @Positive BigDecimal quantity,
    @NotBlank String unit,
    @NotNull @PastOrPresent LocalDate activityDate){
}
