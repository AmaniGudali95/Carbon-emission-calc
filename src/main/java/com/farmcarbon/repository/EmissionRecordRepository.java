package com.farmcarbon.repository;

import com.farmcarbon.entity.EmissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface EmissionRecordRepository extends JpaRepository<EmissionRecord, Long> {
    Optional<EmissionRecord> findByActivityId(Long activityId);


    @Query("SELECT COALESCE(SUM(e.co2eKg),0) FROM EmissionRecord e WHERE e.activity.farm.id = :farmId")
    BigDecimal sumCo2eKgByFarmId(@Param("farmId") Long farmId);
}
