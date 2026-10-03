package com.farmcarbon.sustainabilityCalc;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEnergyEstimator {
    private final MeterRegistry meterRegistry;

    private static final double ESTIMATED_WATTS_PER_CPU_CORE_AT_FULL_LOAD = 15.0;
    private static final double ESTIMATED_IDLE_WATTS_PER_CPU_CORE = 3.0;

    public ApplicationEnergyEstimator(MeterRegistry meterRegistry) {
        this.meterRegistry=meterRegistry;
    }

    public double estimateCumulativeEnergyKwh() {
        double cpuUtilization = getCurrentCpuUtilization();
        int availableProcessors = Runtime.getRuntime().availableProcessors();

        double wattsPerCore = ESTIMATED_IDLE_WATTS_PER_CPU_CORE +
                (cpuUtilization * (ESTIMATED_WATTS_PER_CPU_CORE_AT_FULL_LOAD-ESTIMATED_IDLE_WATTS_PER_CPU_CORE));
        double totalWatts=wattsPerCore*availableProcessors;
        double uptimeHours = getUptimeSeconds() / 3600.0;
        return (totalWatts * uptimeHours) / 1000.0;
    }
    private double getCurrentCpuUtilization() {
        var gauge = meterRegistry.find("process.cpu.usage").gauge();
        return gauge!=null? gauge.value() : 0.0;
    }

    private double getUptimeSeconds() {
        var timer = meterRegistry.find("process.uptime").gauge();
        return timer != null ? timer.value() : 0.0;
    }
}