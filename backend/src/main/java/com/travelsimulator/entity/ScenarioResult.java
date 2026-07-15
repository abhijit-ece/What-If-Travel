package com.travelsimulator.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scenario_results")
public class ScenarioResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @Column(name = "budget_projection")
    private Double budgetProjection;

    @Column(name = "hidden_expenses")
    private Double hiddenExpenses;

    @Column(name = "weather_summary", length = 255)
    private String weatherSummary;

    @Column(name = "crowd_level")
    private Integer crowdLevel;

    @Column(name = "safety_score")
    private Integer safetyScore;

    @Column(name = "travel_time_estimate", length = 100)
    private String travelTimeEstimate;

    @Column(name = "itinerary_json", columnDefinition = "TEXT")
    private String itineraryJson;

    @Column(name = "decision_score")
    private Double decisionScore;

    @Column(name = "decision_breakdown_json", columnDefinition = "TEXT")
    private String decisionBreakdownJson;

    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    public ScenarioResult() {}

    public ScenarioResult(Scenario scenario) {
        this.scenario = scenario;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public Double getBudgetProjection() {
        return budgetProjection;
    }

    public void setBudgetProjection(Double budgetProjection) {
        this.budgetProjection = budgetProjection;
    }

    public Double getHiddenExpenses() {
        return hiddenExpenses;
    }

    public void setHiddenExpenses(Double hiddenExpenses) {
        this.hiddenExpenses = hiddenExpenses;
    }

    public String getWeatherSummary() {
        return weatherSummary;
    }

    public void setWeatherSummary(String weatherSummary) {
        this.weatherSummary = weatherSummary;
    }

    public Integer getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(Integer crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public Integer getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(Integer safetyScore) {
        this.safetyScore = safetyScore;
    }

    public String getTravelTimeEstimate() {
        return travelTimeEstimate;
    }

    public void setTravelTimeEstimate(String travelTimeEstimate) {
        this.travelTimeEstimate = travelTimeEstimate;
    }

    public String getItineraryJson() {
        return itineraryJson;
    }

    public void setItineraryJson(String itineraryJson) {
        this.itineraryJson = itineraryJson;
    }

    public Double getDecisionScore() {
        return decisionScore;
    }

    public void setDecisionScore(Double decisionScore) {
        this.decisionScore = decisionScore;
    }

    public String getDecisionBreakdownJson() {
        return decisionBreakdownJson;
    }

    public void setDecisionBreakdownJson(String decisionBreakdownJson) {
        this.decisionBreakdownJson = decisionBreakdownJson;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}
