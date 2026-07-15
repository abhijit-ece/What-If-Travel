package com.travelsimulator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_scenario_sessions")
public class TripScenarioSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "base_trip_json", columnDefinition = "TEXT", nullable = false)
    private String baseTripJson;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scenario> scenarios = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public TripScenarioSession() {}

    public TripScenarioSession(User user, String baseTripJson) {
        this.user = user;
        this.baseTripJson = baseTripJson;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBaseTripJson() {
        return baseTripJson;
    }

    public void setBaseTripJson(String baseTripJson) {
        this.baseTripJson = baseTripJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public void addScenario(Scenario scenario) {
        scenarios.add(scenario);
        scenario.setSession(this);
    }

    public void removeScenario(Scenario scenario) {
        scenarios.remove(scenario);
        scenario.setSession(null);
    }
}
