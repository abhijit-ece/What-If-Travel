package com.travelsimulator.service;

import com.travelsimulator.dto.DecisionScoreBreakdown;
import com.travelsimulator.dto.WeatherResponse;
import com.travelsimulator.dto.DistanceResponse;
import com.travelsimulator.dto.AiReasoningResponse;
import com.travelsimulator.entity.Scenario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class DecisionScoreEngine {

    private static final Logger logger = LoggerFactory.getLogger(DecisionScoreEngine.class);

    /**
     * Computes the transparent, explainable 0–100 decision score for a scenario.
     */
    public DecisionScoreBreakdown calculateDecisionScore(Scenario scenario, 
                                                          WeatherResponse weather, 
                                                          DistanceResponse distance, 
                                                          AiReasoningResponse reasoning) {
        
        int month = scenario.getStartDate().getMonthValue();
        int groupSize = getGroupSize(scenario.getGroupType());

        // 1. Budget Fit (25%)
        double budgetFit = calculateBudgetFit(
            scenario.getBudget(), 
            distance.getDistanceKm(), 
            distance.getDurationHours(), 
            groupSize, 
            scenario.getTravelMode(), 
            reasoning.getHiddenExpenseEstimate()
        );

        // 2. Weather Suitability (20%)
        double weatherSuitability = calculateWeatherSuitability(weather.getTemp(), weather.getDescription());

        // 3. Crowd Level Score (15%) - Inverse of crowd density
        double crowdLevelScore = calculateCrowdLevelScore(reasoning.getCrowdLevelEstimate(), month);

        // 4. Safety (15%)
        double safety = calculateSafetyScore(scenario.getDestination(), month, reasoning.getSafetyNotes());

        // 5. Travel Convenience (10%)
        double convenience = calculateConvenienceScore(scenario.getTravelMode(), distance.getDurationHours(), scenario.getGroupType());

        // 6. Hidden Expense Risk Score (10%) - Inverse of hidden cost impact
        double hiddenExpenseRiskScore = calculateHiddenExpenseRiskScore(reasoning.getHiddenExpenseEstimate(), scenario.getBudget());

        // 7. Preference Match (5%)
        double preferenceMatch = calculatePreferenceMatch(scenario.getDestination(), scenario.getGroupType());

        // Combine weighted score:
        double finalScore = (budgetFit * 0.25)
                          + (weatherSuitability * 0.20)
                          + (crowdLevelScore * 0.15)
                          + (safety * 0.15)
                          + (convenience * 0.10)
                          + (hiddenExpenseRiskScore * 0.10)
                          + (preferenceMatch * 0.05);

        // Round final score to 1 decimal place
        finalScore = Math.round(finalScore * 10.0) / 10.0;

        logger.info("Decision score calculated: {}/100 for label: '{}'", finalScore, scenario.getLabel());

        return new DecisionScoreBreakdown(
            Math.round(budgetFit * 10.0) / 10.0,
            Math.round(weatherSuitability * 10.0) / 10.0,
            Math.round(crowdLevelScore * 10.0) / 10.0,
            Math.round(safety * 10.0) / 10.0,
            Math.round(convenience * 10.0) / 10.0,
            Math.round(hiddenExpenseRiskScore * 10.0) / 10.0,
            Math.round(preferenceMatch * 10.0) / 10.0,
            finalScore
        );
    }

    private double calculateBudgetFit(double statedBudget, double distanceKm, double durationHours, 
                                       int groupSize, String travelMode, double hiddenExpenses) {
        
        // Estimate travel fare based on mode, distance and group size
        double travelRate = 2.5; // default Car rate per km per group
        if ("flight".equalsIgnoreCase(travelMode)) {
            travelRate = 5.0; // rate per km per person for flights
        } else if ("train".equalsIgnoreCase(travelMode)) {
            travelRate = 1.2; // rate per km per person for trains
        }

        double travelFare = distanceKm * travelRate;
        if (!"car".equalsIgnoreCase(travelMode)) {
            travelFare = travelFare * groupSize; // scale flight/train by travelers
        }

        // Estimate lodging/meals cost based on trip days (defaulting to 4 days if date range is empty)
        double lodgingDays = Math.max(1.0, durationHours / 24.0);
        int days = (int) Math.round(lodgingDays);
        if (days < 2) days = 4; // default base duration estimate

        double lodgingCost = 2500.0 * groupSize * days;
        double estimatedTotal = travelFare + lodgingCost + hiddenExpenses;

        double ratio = estimatedTotal / statedBudget;

        double score;
        if (ratio <= 1.0) {
            score = 100.0;
        } else {
            // Exceeding budget drops the score proportionally
            score = 100.0 - (ratio - 1.0) * 150.0;
        }

        // Penalty for high hidden expense ratios
        double hiddenPenalty = (hiddenExpenses / statedBudget) * 100.0;
        score = score - hiddenPenalty;

        return Math.max(0.0, Math.min(100.0, score));
    }

    private double calculateWeatherSuitability(double temp, String description) {
        double tempScore = 100.0;

        // Optimal weather temperature: 20C - 28C
        if (temp > 28.0) {
            tempScore = 100.0 - (temp - 28.0) * 4.0;
        } else if (temp < 20.0) {
            tempScore = 100.0 - (20.0 - temp) * 4.0;
        }

        tempScore = Math.max(0.0, Math.min(100.0, tempScore));

        double penalty = 0.0;
        String descLower = description.toLowerCase();

        if (descLower.contains("monsoon") || descLower.contains("heavy rain") || descLower.contains("thunderstorm")) {
            penalty = 50.0;
        } else if (descLower.contains("snow") || descLower.contains("extreme cold")) {
            penalty = 40.0;
        } else if (descLower.contains("moderate rain") || descLower.contains("shower")) {
            penalty = 25.0;
        } else if (descLower.contains("light rain") || descLower.contains("drizzle")) {
            penalty = 12.0;
        } else if (descLower.contains("clear") || descLower.contains("sunny")) {
            penalty = -10.0; // 10 point bonus for ideal sunshine weather
        }

        return Math.max(0.0, Math.min(100.0, tempScore - penalty));
    }

    private double calculateCrowdLevelScore(int crowdLevel, int month) {
        double density = crowdLevel;

        // Peak tourist holiday multiplier (Dec, Jan, May, June)
        if (month == 12 || month == 1 || month == 5 || month == 6) {
            density = Math.min(100.0, density * 1.25);
        }

        // Higher crowd lowers the suitability score
        return Math.max(0.0, 100.0 - density);
    }

    private double calculateSafetyScore(String destination, int month, String safetyNotes) {
        double score = 100.0;
        String destLower = destination.toLowerCase();

        // Monsoon hazard checks (June - Sept)
        if (month >= 6 && month <= 9) {
            if (destLower.contains("goa") || destLower.contains("kerala") || destLower.contains("kochi")) {
                score -= 30.0; // Rough seas, beach swimming warnings
            } else if (destLower.contains("himalaya") || destLower.contains("manali") || destLower.contains("shimla") || destLower.contains("leh")) {
                score -= 40.0; // High risk of mudslides & highway washouts
            }
        }

        // Winter blizzard checks (Dec - Feb)
        if (month == 12 || month == 1 || month == 2) {
            if (destLower.contains("himalaya") || destLower.contains("manali") || destLower.contains("shimla") || destLower.contains("leh")) {
                score -= 35.0; // High risk of blocked passes & freezing temperatures
            }
        }

        // Check keyword indicators in safety notes
        if (safetyNotes != null) {
            String notesLower = safetyNotes.toLowerCase();
            if (notesLower.contains("landslide") || notesLower.contains("flood") || notesLower.contains("blizzard")) {
                score -= 20.0;
            }
            if (notesLower.contains("scam") || notesLower.contains("theft") || notesLower.contains("pickpocket")) {
                score -= 10.0;
            }
            if (notesLower.contains("closure") || notesLower.contains("closed")) {
                score -= 10.0;
            }
        }

        return Math.max(0.0, score);
    }

    private double calculateConvenienceScore(String travelMode, double durationHours, String groupType) {
        double score = 50.0;

        if ("flight".equalsIgnoreCase(travelMode)) {
            score = 90.0;
        } else if ("train".equalsIgnoreCase(travelMode)) {
            score = 65.0;
        } else if ("car".equalsIgnoreCase(travelMode)) {
            score = 45.0;
        }

        // Duration penalty
        double penalty = 0.0;
        if (durationHours > 24.0) {
            penalty = 25.0;
        } else if (durationHours > 12.0) {
            penalty = 15.0;
        } else if (durationHours > 6.0) {
            penalty = 5.0;
        }

        score = score - penalty;

        // Group modifiers
        double modifier = 0.0;
        if ("family".equalsIgnoreCase(groupType)) {
            if ("car".equalsIgnoreCase(travelMode)) {
                modifier = -15.0; // Seniors/kids suffer on long road trips
            } else if ("flight".equalsIgnoreCase(travelMode)) {
                modifier = 5.0; // Flights are easiest for families
            }
        } else if ("friends".equalsIgnoreCase(groupType)) {
            if ("car".equalsIgnoreCase(travelMode)) {
                modifier = 5.0; // Road trip bonus for friends
            }
        }

        return Math.max(0.0, Math.min(100.0, score + modifier));
    }

    private double calculateHiddenExpenseRiskScore(double hiddenExpenses, double statedBudget) {
        double ratio = (hiddenExpenses / statedBudget) * 100.0;
        // Higher relative hidden costs lower the score
        return Math.max(0.0, 100.0 - ratio);
    }

    private double calculatePreferenceMatch(String destination, String groupType) {
        String destLower = destination.toLowerCase();
        String groupLower = groupType.toLowerCase();

        if (destLower.contains("goa")) {
            if ("friends".equals(groupLower)) return 100.0;
            if ("couple".equals(groupLower)) return 95.0;
            if ("solo".equals(groupLower)) return 90.0;
            if ("family".equals(groupLower)) return 75.0;
        } else if (destLower.contains("manali") || destLower.contains("shimla") || destLower.contains("leh")) {
            if ("solo".equals(groupLower)) return 95.0;
            if ("friends".equals(groupLower)) return 95.0;
            if ("couple".equals(groupLower)) return 90.0;
            if ("family".equals(groupLower)) return 55.0; // hard treks for children/elders
        } else if (destLower.contains("jaipur")) {
            if ("family".equals(groupLower)) return 95.0;
            if ("couple".equals(groupLower)) return 90.0;
            if ("solo".equals(groupLower)) return 80.0;
            if ("friends".equals(groupLower)) return 80.0;
        }

        return 80.0; // Default compatibility
    }

    private int getGroupSize(String groupType) {
        switch (groupType.toLowerCase()) {
            case "solo": return 1;
            case "couple": return 2;
            case "family": return 4; // average size
            case "friends": return 3; // average size
            default: return 1;
        }
    }
}
