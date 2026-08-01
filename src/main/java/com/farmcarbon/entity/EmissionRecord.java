package com.farmcarbon.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "emission_records")
@Getter
@Setter
@NoArgsConstructor
public class EmissionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private Activity activity;

    @Column(name = "co2e_kg", nullable=false)
    private BigDecimal co2eKg;

    @Column(name = "emission_factor_source", nullable = false)
    private String emissionFactorSource;

    @Column(name = "calculated_at", nullable = false, updatable = false)
    private Instant calculatedAt;

    @PrePersist
    void onCreate(){
        this.calculatedAt = Instant.now();
    }

    public EmissionRecord(Activity activity, BigDecimal co2eKg, String emissionFactorSource) {
        this.activity=activity;
        this.co2eKg=co2eKg;
        this.emissionFactorSource=emissionFactorSource;
    }
}
