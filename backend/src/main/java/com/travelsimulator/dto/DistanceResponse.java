package com.travelsimulator.dto;

public class DistanceResponse {

    private Double distanceKm;
    private Double durationHours;
    private String travelTimeEstimate; // e.g. "2 hours 15 mins" or "1.5 days"
    private boolean approximation;

    public DistanceResponse() {}

    public DistanceResponse(Double distanceKm, Double durationHours, String travelTimeEstimate, boolean approximation) {
        this.distanceKm = distanceKm;
        this.durationHours = durationHours;
        this.travelTimeEstimate = travelTimeEstimate;
        this.approximation = approximation;
    }

    // Getters and Setters
    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Double getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Double durationHours) {
        this.durationHours = durationHours;
    }

    public String getTravelTimeEstimate() {
        return travelTimeEstimate;
    }

    public void setTravelTimeEstimate(String travelTimeEstimate) {
        this.travelTimeEstimate = travelTimeEstimate;
    }

    public boolean isApproximation() {
        return approximation;
    }

    public void setApproximation(boolean approximation) {
        this.approximation = approximation;
    }
}
