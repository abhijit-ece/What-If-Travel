package com.travelsimulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ScenarioSessionRequest {

    @NotBlank
    private String destination;

    @NotBlank
    private String startDate; // yyyy-MM-dd

    @NotNull
    private Double budget;

    @NotBlank
    private String groupType;

    @NotBlank
    private String travelMode;

    private List<String> whatIfs;

    public ScenarioSessionRequest() {}

    public ScenarioSessionRequest(String destination, String startDate, Double budget, String groupType, String travelMode, List<String> whatIfs) {
        this.destination = destination;
        this.startDate = startDate;
        this.budget = budget;
        this.groupType = groupType;
        this.travelMode = travelMode;
        this.whatIfs = whatIfs;
    }

    // Getters and Setters
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

    public List<String> getWhatIfs() {
        return whatIfs;
    }

    public void setWhatIfs(List<String> whatIfs) {
        this.whatIfs = whatIfs;
    }
}
