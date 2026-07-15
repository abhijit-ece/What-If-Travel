package com.travelsimulator.dto;

public class WeatherResponse {

    private Double temp;
    private Double feelsLike;
    private Integer humidity;
    private String description;
    private Double windSpeed;
    private boolean apiSuccess;
    private String errorMessage;
    private boolean isLive; // true if live API, false if historical average fallback

    public WeatherResponse() {}

    // Success Constructor
    public WeatherResponse(Double temp, Double feelsLike, Integer humidity, String description, Double windSpeed, boolean isLive) {
        this.temp = temp;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.description = description;
        this.windSpeed = windSpeed;
        this.apiSuccess = true;
        this.isLive = isLive;
    }

    // Error Constructor
    public WeatherResponse(String errorMessage) {
        this.apiSuccess = false;
        this.errorMessage = errorMessage;
        this.isLive = false;
    }

    // Getters and Setters
    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(Double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public boolean isApiSuccess() {
        return apiSuccess;
    }

    public void setApiSuccess(boolean apiSuccess) {
        this.apiSuccess = apiSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }
}
