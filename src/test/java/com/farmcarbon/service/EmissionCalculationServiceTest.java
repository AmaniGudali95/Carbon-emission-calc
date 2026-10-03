package com.farmcarbon.service;

import com.farmcarbon.client.CarbonInterfaceClient;
import com.farmcarbon.entity.Activity;
import com.farmcarbon.entity.ActivityType;
import com.farmcarbon.entity.EmissionRecord;
import com.farmcarbon.repository.EmissionRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmissionCalculationServiceTest {

    @Mock
    private CarbonInterfaceClient carbonInterfaceClient;

    @Spy
    private final EmissionFactorCalculator emissionFactorCalculator = new EmissionFactorCalculator();

    @Mock
    private EmissionRecordRepository emissionRecordRepository;

    @InjectMocks
    private EmissionCalculationService emissionCalculationService;

    @Test
    void fuelCombustionCallsCarbonInterface() {
        Activity activity = activityOf(ActivityType.FUEL_COMBUSTION, BigDecimal.valueOf(50));

        when(carbonInterfaceClient.estimateFuelCombustionCo2eKg(anyDouble())).thenReturn(BigDecimal.valueOf(132.5));
        stubRepositorySave();

        EmissionRecord result = emissionCalculationService.calculateAndSave(activity);

        assertThat(result.getCo2eKg()).isEqualByComparingTo(BigDecimal.valueOf(132.5));
        assertThat(result.getEmissionFactorSource()).isEqualTo("Carbon Interface");
        verify(carbonInterfaceClient).estimateFuelCombustionCo2eKg(50.0);
    }

    @Test
    void irrigationEnergyCallsCarbonInterfaceElectricityEndpoint() {
        Activity activity = activityOf(ActivityType.IRRIGATION_ENERGY, BigDecimal.valueOf(20));

        when(carbonInterfaceClient.estimateElectricityCo2eKg(anyDouble())).thenReturn(BigDecimal.valueOf(8.4));

        stubRepositorySave();

        EmissionRecord result = emissionCalculationService.calculateAndSave(activity);

        assertThat(result.getCo2eKg()).isEqualByComparingTo(BigDecimal.valueOf(8.4));
        verify(carbonInterfaceClient).estimateElectricityCo2eKg(20.0);
    }

    @Test
    void fertilizerUsesStaticFactorNotCarbonInterface() {
        Activity activity = activityOf(ActivityType.FERTILIZER_APPLICATION, BigDecimal.valueOf(100));
        stubRepositorySave();

        EmissionRecord result = emissionCalculationService.calculateAndSave(activity);

        assertThat(result.getCo2eKg()).isEqualByComparingTo(BigDecimal.valueOf(442));

        assertThat(result.getEmissionFactorSource()).isEqualTo("IPCC default factor");

        verifyNoInteractions(carbonInterfaceClient);
    }

    @Test
    void livestockUsesStaticFactor() {
        Activity activity = activityOf(ActivityType.LIVESTOCK, BigDecimal.valueOf(5));
        stubRepositorySave();

        EmissionRecord result = emissionCalculationService.calculateAndSave(activity);

        assertThat(result.getCo2eKg()).isEqualByComparingTo(BigDecimal.valueOf(9000));
        verifyNoInteractions(carbonInterfaceClient);
    }

    @Test
    void tillageProducesNegativeEmissions() {
        Activity activity = activityOf(ActivityType.TILLAGE, BigDecimal.valueOf(12.5));
        stubRepositorySave();

        EmissionRecord result = emissionCalculationService.calculateAndSave(activity);

        assertThat(result.getCo2eKg()).isEqualByComparingTo(BigDecimal.valueOf(-1875));

    }
    
    @Test
    void savesTheCalculatedRecordExactlyOnce() {
        Activity activity = activityOf(ActivityType.FERTILIZER_APPLICATION, BigDecimal.valueOf(10));
        stubRepositorySave();

        emissionCalculationService.calculateAndSave(activity);

        verify(emissionRecordRepository, times(1)).save(any(EmissionRecord.class));
    }



    private Activity  activityOf(ActivityType type, BigDecimal quantity) {
        return new Activity(null, type, quantity, "unit", LocalDate.now());
    }

    private void stubRepositorySave() {
        when(emissionRecordRepository.save(any(EmissionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

}
