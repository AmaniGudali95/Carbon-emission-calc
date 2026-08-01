package com.farmcarbon.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.jar.Attributes;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CarbonInterfaceResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(Attributes attributes) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attributes(double carbon_kg) {}
}
