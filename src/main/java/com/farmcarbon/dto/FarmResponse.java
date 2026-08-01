package com.farmcarbon.dto;

import com.farmcarbon.entity.Farm;

import java.math.BigDecimal;

public record FarmResponse(Long id, String name, String location, BigDecimal sizeHectares, String cropType){
    public static FarmResponse from(Farm farm){
        return new FarmResponse(farm.getId(),farm.getName(), farm.getLocation(), farm.getSizeHectares(), farm.getCropType());
    }
}