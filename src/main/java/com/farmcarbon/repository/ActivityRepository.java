package com.farmcarbon.repository;

import com.farmcarbon.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByFarmId(Long farmId);
}
