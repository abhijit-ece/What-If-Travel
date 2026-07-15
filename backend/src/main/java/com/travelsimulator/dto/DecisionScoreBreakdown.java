package com.travelsimulator.dto;

public class DecisionScoreBreakdown {

    private Double budgetFit;
    private Double weatherSuitability;
    private Double crowdLevelScore;
    private Double safety;
    private Double convenience;
    private Double hiddenExpenseRiskScore;
    private Double preferenceMatch;
    private Double finalScore;

    public DecisionScoreBreakdown() {}

    public DecisionScoreBreakdown(Double budgetFit, Double weatherSuitability, Double crowdLevelScore, 
                                  Double safety, Double convenience, Double hiddenExpenseRiskScore, 
                                  Double preferenceMatch, Double finalScore) {
        this.budgetFit = budgetFit;
        this.weatherSuitability = weatherSuitability;
        this.crowdLevelScore = crowdLevelScore;
        this.safety = safety;
        this.convenience = convenience;
        this.hiddenExpenseRiskScore = hiddenExpenseRiskScore;
        this.preferenceMatch = preferenceMatch;
        this.finalScore = finalScore;
    }

    // Getters and Setters
    public Double getBudgetFit() {
        return budgetFit;
    }

    public void setBudgetFit(Double budgetFit) {
        this.budgetFit = budgetFit;
    }

    public Double getWeatherSuitability() {
        return weatherSuitability;
    }

    public void setWeatherSuitability(Double weatherSuitability) {
        this.weatherSuitability = weatherSuitability;
    }

    public Double getCrowdLevelScore() {
        return crowdLevelScore;
    }

    public void setCrowdLevelScore(Double crowdLevelScore) {
        this.crowdLevelScore = crowdLevelScore;
    }

    public Double getSafety() {
        return safety;
    }

    public void setSafety(Double safety) {
        this.safety = safety;
    }

    public Double getConvenience() {
        return convenience;
    }

    public void setConvenience(Double convenience) {
        this.convenience = convenience;
    }

    public Double getHiddenExpenseRiskScore() {
        return hiddenExpenseRiskScore;
    }

    public void setHiddenExpenseRiskScore(Double hiddenExpenseRiskScore) {
        this.hiddenExpenseRiskScore = hiddenExpenseRiskScore;
    }

    public Double getPreferenceMatch() {
        return preferenceMatch;
    }

    public void setPreferenceMatch(Double preferenceMatch) {
        this.preferenceMatch = preferenceMatch;
    }

    public Double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }
}
