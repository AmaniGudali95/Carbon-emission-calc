package com.farmcarbon.sustainabilityCalc;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sustainability")
public class SustainabilityController {

    private final ApplicationFootprintService applicationFootprintService;
    private final CombinedApplicationFootprintService combinedApplicationFootprintService;

    public SustainabilityController(ApplicationFootprintService applicationFootprintService,
                                    CombinedApplicationFootprintService combinedApplicationFootprintService) {
        this.applicationFootprintService = applicationFootprintService;
        this.combinedApplicationFootprintService = combinedApplicationFootprintService;
    }

    @GetMapping("/footprint")
    public ApplicationFootprintService.ApplicationFootprint getOwnFootprint() {
        return applicationFootprintService.calculateCurrentFootprint();
    }

    @GetMapping("/footprint/combined")
    public CombinedApplicationFootprintService.CombinedFootprint getCombinedFootprint() {
        return combinedApplicationFootprintService.calculateCombinedFootprint();
    }
}
