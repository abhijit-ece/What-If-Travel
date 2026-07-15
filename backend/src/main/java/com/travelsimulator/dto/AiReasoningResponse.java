package com.travelsimulator.dto;

import java.util.List;

public class AiReasoningResponse {

    private Integer crowdLevelEstimate;
    private String safetyNotes;
    private Double hiddenExpenseEstimate;
    private List<ItineraryDay> itinerary;
    private List<String> alternativeLocations;
    private String reasoningExplanation;

    public AiReasoningResponse() {}

    public AiReasoningResponse(Integer crowdLevelEstimate, String safetyNotes, Double hiddenExpenseEstimate, 
                               List<ItineraryDay> itinerary, List<String> alternativeLocations, String reasoningExplanation) {
        this.crowdLevelEstimate = crowdLevelEstimate;
        this.safetyNotes = safetyNotes;
        this.hiddenExpenseEstimate = hiddenExpenseEstimate;
        this.itinerary = itinerary;
        this.alternativeLocations = alternativeLocations;
        this.reasoningExplanation = reasoningExplanation;
    }

    // Getters and Setters
    public Integer getCrowdLevelEstimate() {
        return crowdLevelEstimate;
    }

    public void setCrowdLevelEstimate(Integer crowdLevelEstimate) {
        this.crowdLevelEstimate = crowdLevelEstimate;
    }

    public String getSafetyNotes() {
        return safetyNotes;
    }

    public void setSafetyNotes(String safetyNotes) {
        this.safetyNotes = safetyNotes;
    }

    public Double getHiddenExpenseEstimate() {
        return hiddenExpenseEstimate;
    }

    public void setHiddenExpenseEstimate(Double hiddenExpenseEstimate) {
        this.hiddenExpenseEstimate = hiddenExpenseEstimate;
    }

    public List<ItineraryDay> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryDay> itinerary) {
        this.itinerary = itinerary;
    }

    public List<String> getAlternativeLocations() {
        return alternativeLocations;
    }

    public void setAlternativeLocations(List<String> alternativeLocations) {
        this.alternativeLocations = alternativeLocations;
    }

    public String getReasoningExplanation() {
        return reasoningExplanation;
    }

    public void setReasoningExplanation(String reasoningExplanation) {
        this.reasoningExplanation = reasoningExplanation;
    }

    // Static nested class for day-wise itinerary
    public static class ItineraryDay {
        private int day;
        private String title;
        private List<String> activities;

        public ItineraryDay() {}

        public ItineraryDay(int day, String title, List<String> activities) {
            this.day = day;
            this.title = title;
            this.activities = activities;
        }

        // Getters and Setters
        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public List<String> getActivities() {
            return activities;
        }

        public void setActivities(List<String> activities) {
            this.activities = activities;
        }
    }
}
