package com.farmcarbon.controller;


import com.farmcarbon.dto.FarmReportResponse;
import com.farmcarbon.entity.Farm;
import com.farmcarbon.repository.EmissionRecordRepository;
import com.farmcarbon.repository.FarmRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/farms/{farmId}/report")
public class ReportController {
    private final FarmRepository farmRepository;
    private final EmissionRecordRepository emissionRecordRepository;

    public ReportController(FarmRepository farmRepository, EmissionRecordRepository emissionRecordRepository){
        this.farmRepository=farmRepository;
        this.emissionRecordRepository=emissionRecordRepository;
    }

    @GetMapping
    public FarmReportResponse getReport(@PathVariable Long farmId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm " + farmId + " not found"));
        BigDecimal totalCo2eKg = emissionRecordRepository.sumCo2eKgByFarmId(farmId);
        return FarmReportResponse.of(farmId, farm.getName(), totalCo2eKg, farm.getSizeHectares());
    }

}
