package com.farmcarbon.dto;

import com.farmcarbon.entity.Activity;
import com.farmcarbon.entity.ActivityType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ActivityResponse (Long id, Long farmId, ActivityType activityType, BigDecimal quantity, String unit, LocalDate activityDate){
    public static ActivityResponse from(Activity activity) {
        return new ActivityResponse(activity.getId(),
                activity.getFarm().getId(),
                activity.getActivityType(),
                activity.getQuantity(),
                activity.getUnit(),
                activity.getActivityDate());
    }
}
