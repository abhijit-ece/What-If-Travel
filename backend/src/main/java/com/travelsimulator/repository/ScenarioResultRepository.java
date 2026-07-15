package com.travelsimulator.repository;

import com.travelsimulator.entity.ScenarioResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScenarioResultRepository extends JpaRepository<ScenarioResult, Long> {
    Optional<ScenarioResult> findByScenarioId(Long scenarioId);
}
