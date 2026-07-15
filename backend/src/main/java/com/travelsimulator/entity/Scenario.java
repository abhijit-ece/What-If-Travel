package com.travelsimulator.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "scenarios")
public class Scenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private TripScenarioSession session;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private Double budget;

    @Column(name = "group_type", nullable = false, length = 50)
    private String groupType;

    @Column(name = "travel_mode", nullable = false, length = 50)
    private String travelMode;

    @Column(name = "is_base_scenario", nullable = false)
    private boolean isBaseScenario;

    @OneToOne(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ScenarioResult result;

    public Scenario() {}

    public Scenario(TripScenarioSession session, String label, String destination, LocalDate startDate, Double budget, String groupType, String travelMode, boolean isBaseScenario) {
        this.session = session;
        this.label = label;
        this.destination = destination;
        this.startDate = startDate;
        this.budget = budget;
        this.groupType = groupType;
        this.travelMode = travelMode;
        this.isBaseScenario = isBaseScenario;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TripScenarioSession getSession() {
        return session;
    }

    public void setSession(TripScenarioSession session) {
        this.session = session;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
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

    public ScenarioResult getResult() {
        return result;
    }

    public void setResult(ScenarioResult result) {
        this.result = result;
        if (result != null && result.getScenario() != this) {
            result.setScenario(this);
        }
    }
}
