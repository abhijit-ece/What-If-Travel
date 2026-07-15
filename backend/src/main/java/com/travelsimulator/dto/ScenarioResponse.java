package com.travelsimulator.dto;

public class ScenarioResponse {

    private Long id;
    private String label;
    private String destination;
    private String startDate;
    private Double budget;
    private String groupType;
    private String travelMode;
    private boolean isBaseScenario;
    private boolean hasResult;

    // Result fields (optional, populated after compute)
    private Double budgetProjection;
    private Double hiddenExpenses;
    private String weatherSummary;
    private Integer crowdLevel;
    private Integer safetyScore;
    private String travelTimeEstimate;
    private String itineraryJson;
    private Double decisionScore;
    private String decisionBreakdownJson;
    private String aiExplanation;

    public ScenarioResponse() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }

    public boolean isBaseScenario() {
        return isBaseScenario;
    }

    public void setBaseScenario(boolean baseScenario) {
        isBaseScenario = baseScenario;
    }

    public boolean isHasResult() {
        return hasResult;
    }

    public void setHasResult(boolean hasResult) {
        this.hasResult = hasResult;
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
