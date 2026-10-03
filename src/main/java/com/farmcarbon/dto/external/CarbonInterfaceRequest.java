package com.farmcarbon.dto.external;

public record CarbonInterfaceRequest(
        String type,
        String fuel_source_type,
        double fuel_source_unit_type_value,
        String fuel_source_unit_type
) {
    public static CarbonInterfaceRequest fuelCombustion(double liters) {
        return new CarbonInterfaceRequest("fuel_combustion", "diesel", liters, "gallon");
    }

    public static CarbonInterfaceRequest electricity(double kWh) {
        return new CarbonInterfaceRequest("electricity", null, kWh, "kWh");
    }
}
