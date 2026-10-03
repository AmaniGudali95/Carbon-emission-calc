package com.farmcarbon.controller;


import com.farmcarbon.dto.ActivityRequest;
import com.farmcarbon.dto.ActivityResponse;
import com.farmcarbon.entity.Activity;
import com.farmcarbon.entity.ActivityType;
import com.farmcarbon.entity.Farm;
import com.farmcarbon.repository.ActivityRepository;
import com.farmcarbon.repository.FarmRepository;
import com.farmcarbon.service.ActivityEventPublisher;
import com.farmcarbon.service.EmissionCalculationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/farms/{farmId}/activities")
public class ActivityController {

    private final ActivityRepository activityRepository;
    private final FarmRepository farmRepository;

    //private final EmissionCalculationService emissionCalculationService;

    private final ActivityEventPublisher activityEventPublisher;

    public ActivityController(ActivityRepository activityRepository,
                              FarmRepository farmRepository,
                              //EmissionCalculationService emissionCalculationService
                              ActivityEventPublisher activityEventPublisher){
        this.activityRepository=activityRepository;
        this.farmRepository=farmRepository;
        this.activityEventPublisher=activityEventPublisher;
        //this.emissionCalculationService = emissionCalculationService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> logActivity(@PathVariable Long farmId, @Valid @RequestBody ActivityRequest request){
        Farm farm = farmRepository.findById(farmId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Farm"+farmId+"not found"));
        Activity activity = new Activity(
                farm,
                request.activityType(),
                request.quantity(),
                request.unit(),
                request.activityDate()
        );

        Activity saved = activityRepository.save(activity);
        //if (saved.getActivityType() == ActivityType.FUEL_COMBUSTION) {
            //emissionCalculationService.calculateAndSave(saved);
        activityEventPublisher.publishActivityLogged(saved.getId());
        //}
        return ResponseEntity.status(HttpStatus.CREATED).body(ActivityResponse.from(saved));
    }

    @GetMapping
    public List<ActivityResponse> listActivities(@PathVariable Long farmId){
        if (!farmRepository.existsById(farmId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm" + farmId + "not found");

        }
        return activityRepository.findByFarmId(farmId).stream().map(ActivityResponse::from).collect(Collectors.toList());
    }

}
