package com.travelsimulator.service;

import com.travelsimulator.dto.WeatherResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WeatherService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;

    public WeatherService(RestTemplate restTemplate, 
                          @Value("${travelsimulator.weather.api-key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    /**
     * Tries to fetch live weather from OpenWeatherMap.
     * If the API call fails or apiKey is blank, it falls back to the seasonal average database.
     */
    @SuppressWarnings("unchecked")
    public WeatherResponse getWeatherData(String destination, int month) {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("your_")) {
            logger.warn("OpenWeatherMap API key is missing or default. Falling back to seasonal/historical averages.");
            return getSeasonalAverage(destination, month);
        }

        try {
            String url = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s", 
                    destination.trim(), apiKey.trim());
            
            logger.info("Calling OpenWeatherMap API for destination: {}", destination);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("main")) {
                Map<String, Object> main = (Map<String, Object>) response.get("main");
                Number temp = (Number) main.get("temp");
                Number feelsLike = (Number) main.get("feels_like");
                Number humidity = (Number) main.get("humidity");

                List<Map<String, Object>> weatherList = (List<Map<String, Object>>) response.get("weather");
                String description = "clear sky";
                if (weatherList != null && !weatherList.isEmpty()) {
                    description = (String) weatherList.get(0).get("description");
                }

                Map<String, Object> wind = (Map<String, Object>) response.get("wind");
                Number windSpeed = 0.0;
                if (wind != null && wind.containsKey("speed")) {
                    windSpeed = (Number) wind.get("speed");
                }

                return new WeatherResponse(
                    temp != null ? temp.doubleValue() : 25.0,
                    feelsLike != null ? feelsLike.doubleValue() : 25.0,
                    humidity != null ? humidity.intValue() : 60,
                    description,
                    windSpeed != null ? windSpeed.doubleValue() : 3.0,
                    true // isLive = true
                );
            }
        } catch (Exception e) {
            logger.error("OpenWeatherMap API request failed for destination: {}. Error: {}", destination, e.getMessage());
            // Section 6 Guardrail: If live API fails, we must NOT silently fake it. 
            // However, we return an error response so the system knows the API call failed, 
            // and we fall back to historical data indicating that it's historical.
            WeatherResponse fallback = getSeasonalAverage(destination, month);
            fallback.setApiSuccess(false);
            fallback.setErrorMessage("Could not fetch live weather: " + e.getMessage() + ". Showing historical climate estimates.");
            return fallback;
        }

        return getSeasonalAverage(destination, month);
    }

    /**
     * Local rule-based climate dictionary providing seasonal estimates for common travel destinations.
     */
    public WeatherResponse getSeasonalAverage(String destination, int month) {
        String destLower = destination.trim().toLowerCase();
        
        // Month boundary validation
        if (month < 1 || month > 12) {
            month = 1;
        }

        double temp = 22.0;
        double feelsLike = 22.0;
        int humidity = 50;
        String desc = "mild weather";
        double windSpeed = 4.0;

        // Simple mock weather profiles for high-interest student demo areas
        if (destLower.contains("goa")) {
            // Monsoon season (June - September)
            if (month >= 6 && month <= 9) {
                temp = 28.0;
                feelsLike = 33.0;
                humidity = 85;
                desc = "heavy monsoon rains, high humidity";
                windSpeed = 7.5;
            } else if (month == 10 || month == 11 || month == 5) {
                temp = 31.0;
                feelsLike = 36.0;
                humidity = 75;
                desc = "hot and humid, light rain possibility";
                windSpeed = 3.5;
            } else { // Winter peak (December - April)
                temp = 26.0;
                feelsLike = 27.0;
                humidity = 60;
                desc = "clear sunny skies, pleasant breeze";
                windSpeed = 4.2;
            }
        } else if (destLower.contains("manali") || destLower.contains("shimla") || destLower.contains("leh") || destLower.contains("himalaya")) {
            // Winter freeze (November - February)
            if (month == 11 || month == 12 || month == 1 || month == 2) {
                temp = 2.0;
                feelsLike = -1.0;
                humidity = 40;
                desc = "extreme cold, heavy snowfall likely";
                windSpeed = 5.0;
            } else if (month >= 7 && month <= 9) {
                temp = 18.0;
                feelsLike = 18.0;
                humidity = 70;
                desc = "monsoon showers, high risk of landslide";
                windSpeed = 3.2;
            } else { // Spring/Autumn
                temp = 14.0;
                feelsLike = 13.0;
                humidity = 45;
                desc = "cool temperature, clear mountain views";
                windSpeed = 4.0;
            }
        } else if (destLower.contains("jaipur") || destLower.contains("rajasthan") || destLower.contains("jodhpur")) {
            // Extreme Summer (April - June)
            if (month >= 4 && month <= 6) {
                temp = 39.0;
                feelsLike = 42.0;
                humidity = 30;
                desc = "extreme dry heatwave, clear skies";
                windSpeed = 6.0;
            } else if (month >= 7 && month <= 9) {
                temp = 31.0;
                feelsLike = 34.0;
                humidity = 65;
                desc = "moderate warm showers, cloudy";
                windSpeed = 4.5;
            } else { // Pleasant winter
                temp = 20.0;
                feelsLike = 20.0;
                humidity = 40;
                desc = "sunny and cool, ideal sightseeing";
                windSpeed = 3.0;
            }
        } else {
            // General global temperate fallback
            if (month >= 6 && month <= 8) { // Summer
                temp = 26.0;
                feelsLike = 27.0;
                humidity = 55;
                desc = "warm summer weather, sunny";
                windSpeed = 3.5;
            } else if (month == 12 || month <= 2) { // Winter
                temp = 8.0;
                feelsLike = 6.0;
                humidity = 65;
                desc = "cold winter weather, overcast";
                windSpeed = 5.0;
            } else { // Spring/Autumn
                temp = 16.0;
                feelsLike = 16.0;
                humidity = 50;
                desc = "pleasant seasonal weather";
                windSpeed = 4.0;
            }
        }

        WeatherResponse response = new WeatherResponse(temp, feelsLike, humidity, desc, windSpeed, false); // isLive = false
        // By default, since this is a successful fallback computation, apiSuccess remains true for calculation purposes.
        response.setApiSuccess(true);
        return response;
    }
}
