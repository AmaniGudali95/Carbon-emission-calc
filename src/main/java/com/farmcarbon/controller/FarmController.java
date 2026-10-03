package com.farmcarbon.controller;


import com.farmcarbon.dto.FarmRequest;
import com.farmcarbon.dto.FarmResponse;
import com.farmcarbon.entity.Farm;
import com.farmcarbon.repository.FarmRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farms")
public class FarmController {
    private final FarmRepository farmRepository;

    public FarmController(FarmRepository farmRepository){
        this.farmRepository=farmRepository;
    }

    @PostMapping
    public ResponseEntity<FarmResponse> createFarm(@Valid @RequestBody FarmRequest request) {
        Farm farm = new Farm(request.name(),
                request.location(), request.sizeHectares(), request.cropType());
        Farm saved=farmRepository.save(farm);
        return ResponseEntity.status(HttpStatus.CREATED).body(FarmResponse.from(saved));

    }

    @GetMapping
    public List<FarmResponse> listFarms() {
        return farmRepository.findAll().stream().map(FarmResponse::from).toList();
    }
}
