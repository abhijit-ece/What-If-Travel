package com.travelsimulator.repository;

import com.travelsimulator.entity.TripScenarioSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripScenarioSessionRepository extends JpaRepository<TripScenarioSession, Long> {
    List<TripScenarioSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
