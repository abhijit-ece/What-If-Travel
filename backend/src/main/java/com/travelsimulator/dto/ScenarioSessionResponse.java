package com.travelsimulator.dto;

import java.util.List;

public class ScenarioSessionResponse {

    private Long id;
    private String baseTripJson;
    private String createdAt;
    private List<ScenarioResponse> scenarios;

    public ScenarioSessionResponse() {}

    public ScenarioSessionResponse(Long id, String baseTripJson, String createdAt, List<ScenarioResponse> scenarios) {
        this.id = id;
        this.baseTripJson = baseTripJson;
        this.createdAt = createdAt;
        this.scenarios = scenarios;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseTripJson() {
        return baseTripJson;
    }

    public void setBaseTripJson(String baseTripJson) {
        this.baseTripJson = baseTripJson;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<ScenarioResponse> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<ScenarioResponse> scenarios) {
        this.scenarios = scenarios;
    }
}
