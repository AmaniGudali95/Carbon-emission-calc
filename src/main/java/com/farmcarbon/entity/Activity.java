package com.farmcarbon.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name="activities")
@Getter
@Setter
@NoArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="farm_id", nullable=false)
    private Farm farm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private BigDecimal quantity;

    @Column(nullable = false)
    private String unit;

    @Column(name="activity_date", nullable=false)
    private LocalDate activityDate;

    @Column(name="created_at", nullable=false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate(){
        this.createdAt=Instant.now();
    }

    public Activity(Farm farm, ActivityType activityType, BigDecimal quantity, String unit, LocalDate activityDate){
        this.farm=farm;
        this.activityType=activityType;
        this.quantity=quantity;
        this.unit=unit;
        this.activityDate=activityDate;
    }
}
