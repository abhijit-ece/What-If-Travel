package com.travelsimulator.service;

import com.travelsimulator.dto.AiReasoningResponse;
import com.travelsimulator.dto.DecisionScoreBreakdown;
import com.travelsimulator.dto.DistanceResponse;
import com.travelsimulator.dto.WeatherResponse;
import com.travelsimulator.entity.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class DecisionScoreEngineTest {

    private DecisionScoreEngine scoreEngine;

    @BeforeEach
    public void setup() {
        scoreEngine = new DecisionScoreEngine();
    }

    @Test
    public void testOptimalScenarioGoaSoloFlightWinter() {
        // Scenario A: Goa, December (Winter), Solo, Flight, Budget 25000 (Very reasonable for solo flight+lodging)
        Scenario scenario = new Scenario(null, "Goa Solo Winter", "Goa", 
                LocalDate.of(2026, 12, 15), 25000.0, "solo", "flight", true);

        // Pleasant winter weather (24C, clear sky)
        WeatherResponse weather = new WeatherResponse(24.0, 24.0, 60, "clear sky", 4.0, true);
        
        // Mumbai to Goa flight (~430km, ~2.5 hrs)
        DistanceResponse distance = new DistanceResponse(430.0, 2.57, "2 hrs 34 mins", true);
        
        // AI returns off-peak/normal crowd and 1500 hidden costs
        AiReasoningResponse reasoning = new AiReasoningResponse(
                45, "Safe season, mild waves", 1500.0, 
                Collections.emptyList(), Collections.emptyList(), "Optimal flight trip"
        );

        DecisionScoreBreakdown breakdown = scoreEngine.calculateDecisionScore(scenario, weather, distance, reasoning);

        assertNotNull(breakdown);
        assertTrue(breakdown.getFinalScore() > 80.0, "Optimal scenario should score high (>80). Score: " + breakdown.getFinalScore());
        assertEquals(94.0, breakdown.getBudgetFit(), "Budget fit should be 94.0 due to 6% hidden expense risk penalty (1500 / 25000).");
        assertTrue(breakdown.getWeatherSuitability() > 90.0, "Weather suitability should be high.");
        assertTrue(breakdown.getSafety() > 90.0, "Safety should be high in winter.");
    }

    @Test
    public void testMonsoonSafetyPenaltyGoaSoloMonsoon() {
        // Scenario B: Goa, July (Monsoon), Solo, Flight, Budget 25000
        Scenario scenario = new Scenario(null, "Goa Solo Monsoon", "Goa", 
                LocalDate.of(2026, 7, 15), 25000.0, "solo", "flight", false);

        // High monsoon rain weather (28C, heavy monsoon rains)
        WeatherResponse weather = new WeatherResponse(28.0, 33.0, 85, "heavy monsoon rains", 8.0, true);
        
        DistanceResponse distance = new DistanceResponse(430.0, 2.57, "2 hrs 34 mins", true);
        
        // AI indicates high safety concerns due to rough seas
        AiReasoningResponse reasoning = new AiReasoningResponse(
                30, "Rough sea warnings, swim restrictions", 3000.0, 
                Collections.emptyList(), Collections.emptyList(), "Monsoon Goa trip"
        );

        DecisionScoreBreakdown breakdown = scoreEngine.calculateDecisionScore(scenario, weather, distance, reasoning);

        assertNotNull(breakdown);
        // Safety should be penalized due to July monsoon in Goa (-30 points)
        assertTrue(breakdown.getSafety() <= 70.0, "Safety should be penalized. Safety: " + breakdown.getSafety());
        // Weather suitability should be penalized due to monsoon rain (-50 points)
        assertTrue(breakdown.getWeatherSuitability() <= 50.0, "Weather suitability should be penalized. Weather: " + breakdown.getWeatherSuitability());
        
        // Final score should reflect these penalties and be lower than Scenario A
        assertTrue(breakdown.getFinalScore() < 80.0, "Monsoon scenario should have lower score. Score: " + breakdown.getFinalScore());
    }

    @Test
    public void testConveniencePenaltyGoaFamilyCar() {
        // Scenario C: Goa, December, Family, Car (Road Trip), Budget 60000
        Scenario scenario = new Scenario(null, "Goa Family Car", "Goa", 
                LocalDate.of(2026, 12, 15), 60000.0, "family", "car", false);

        WeatherResponse weather = new WeatherResponse(26.0, 27.0, 60, "clear sky", 4.0, true);
        
        // Mumbai to Goa road trip: ~600km, ~12 hours driving
        DistanceResponse distance = new DistanceResponse(600.0, 12.0, "12 hrs", true);
        
        AiReasoningResponse reasoning = new AiReasoningResponse(
                60, "No major weather alerts", 4000.0, 
                Collections.emptyList(), Collections.emptyList(), "Family road trip"
        );

        DecisionScoreBreakdown breakdown = scoreEngine.calculateDecisionScore(scenario, weather, distance, reasoning);

        assertNotNull(breakdown);
        // Convenience should be penalized because of family traveling by car for >8 hours
        assertTrue(breakdown.getConvenience() <= 40.0, "Family car convenience should be low. Convenience: " + breakdown.getConvenience());
    }

    @Test
    public void testBudgetPenaltyGoaFriendsOverBudget() {
        // Scenario D: Goa, December, Friends, Flight, Budget 12000 (Extremely low budget for flights + lodging for 3 friends)
        Scenario scenario = new Scenario(null, "Goa Friends Flight", "Goa", 
                LocalDate.of(2026, 12, 15), 12000.0, "friends", "flight", false);

        WeatherResponse weather = new WeatherResponse(26.0, 27.0, 60, "clear sky", 4.0, true);
        
        DistanceResponse distance = new DistanceResponse(430.0, 2.57, "2 hrs 34 mins", true);
        
        // High hidden costs
        AiReasoningResponse reasoning = new AiReasoningResponse(
                70, "Safe", 8000.0, 
                Collections.emptyList(), Collections.emptyList(), "Overbudget friends flight"
        );

        DecisionScoreBreakdown breakdown = scoreEngine.calculateDecisionScore(scenario, weather, distance, reasoning);

        assertNotNull(breakdown);
        // Budget fit should be 0 because projected cost exceeds the stated budget of 12000 significantly
        assertEquals(0.0, breakdown.getBudgetFit(), "Budget fit should be 0 due to heavy over-budget.");
        assertTrue(breakdown.getFinalScore() < 60.0, "Final score should be low due to budget penalty. Score: " + breakdown.getFinalScore());
    }
}
