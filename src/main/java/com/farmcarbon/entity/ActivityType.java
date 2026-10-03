package com.farmcarbon.entity;

public enum ActivityType {
    FERTILIZER_APPLICATION,
    FUEL_COMBUSTION,
    IRRIGATION_ENERGY,
    LIVESTOCK,
    TILLAGE;

    public GhgScope getGhgScope() {
        return this == IRRIGATION_ENERGY ? GhgScope.SCOPE_2 : GhgScope.SCOPE_1;
    }
}
