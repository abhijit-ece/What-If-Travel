package com.travelsimulator.service;

import com.travelsimulator.dto.DistanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DistanceService {

    private static final Logger logger = LoggerFactory.getLogger(DistanceService.class);

    // City coordinate registry
    private static final Map<String, Coordinate> CITY_COORDINATES = new HashMap<>();

    static {
        CITY_COORDINATES.put("mumbai", new Coordinate(19.0760, 72.8777));
        CITY_COORDINATES.put("delhi", new Coordinate(28.7041, 77.1025));
        CITY_COORDINATES.put("bangalore", new Coordinate(12.9716, 77.5946));
        CITY_COORDINATES.put("goa", new Coordinate(15.2993, 74.1240));
        CITY_COORDINATES.put("manali", new Coordinate(32.2396, 77.1887));
        CITY_COORDINATES.put("shimla", new Coordinate(31.1048, 77.1734));
        CITY_COORDINATES.put("jaipur", new Coordinate(26.9124, 75.7873));
        CITY_COORDINATES.put("kerala", new Coordinate(9.9312, 76.2673));
        CITY_COORDINATES.put("kochi", new Coordinate(9.9312, 76.2673));
        CITY_COORDINATES.put("paris", new Coordinate(48.8566, 2.3522));
        CITY_COORDINATES.put("tokyo", new Coordinate(35.6762, 139.6503));
        CITY_COORDINATES.put("london", new Coordinate(51.5074, -0.1278));
        CITY_COORDINATES.put("new york", new Coordinate(40.7128, -74.0060));
        CITY_COORDINATES.put("singapore", new Coordinate(1.3521, 103.8198));
    }

    /**
     * Calculates distance and estimated travel time between an origin and destination.
     * Defaults to Mumbai if origin is not found.
     */
    public DistanceResponse estimateDistanceAndDuration(String origin, String destination, String travelMode) {
        String originKey = origin != null ? origin.trim().toLowerCase() : "mumbai";
        String destKey = destination != null ? destination.trim().toLowerCase() : "goa";
        String mode = travelMode != null ? travelMode.trim().toLowerCase() : "flight";

        Coordinate originCoord = findCoordinate(originKey, new Coordinate(19.0760, 72.8777)); // default Mumbai
        Coordinate destCoord = findCoordinate(destKey, new Coordinate(15.2993, 74.1240)); // default Goa

        double distanceKm = haversine(originCoord.lat, originCoord.lon, destCoord.lat, destCoord.lon);
        
        // Adjust for road route curvature (direct line distance is shorter than road/rail)
        if (mode.equals("car") || mode.equals("train")) {
            distanceKm = distanceKm * 1.25; // 25% curve adjustment
        }

        double durationHours;
        String travelTimeText;

        switch (mode) {
            case "flight":
                // Average flight speed: 750 km/h + 2 hours airport check-in/boarding overhead
                durationHours = (distanceKm / 750.0) + 2.0;
                break;
            case "train":
                // Average express train speed: 65 km/h + 1 hour boarding overhead
                durationHours = (distanceKm / 65.0) + 1.0;
                break;
            case "car":
            case "road":
            default:
                // Average driving speed: 55 km/h + rest stops overhead (1 hr rest for every 4 hrs driving)
                double driveHours = distanceKm / 55.0;
                double restStops = Math.floor(driveHours / 4.0);
                durationHours = driveHours + restStops;
                break;
        }

        travelTimeText = formatDuration(durationHours);

        logger.info("Estimated distance: {} km, travel time: {} via {}", distanceKm, travelTimeText, mode);
        return new DistanceResponse(distanceKm, durationHours, travelTimeText, true); // true = approximation
    }

    private Coordinate findCoordinate(String cityQuery, Coordinate defaultCoord) {
        for (Map.Entry<String, Coordinate> entry : CITY_COORDINATES.entrySet()) {
            if (cityQuery.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultCoord;
    }

    /**
     * Great-circle distance using Haversine formula
     */
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth's radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private String formatDuration(double hours) {
        if (hours >= 24) {
            double days = hours / 24.0;
            return String.format("%.1f days", days);
        }
        
        int hrs = (int) hours;
        int mins = (int) Math.round((hours - hrs) * 60);
        
        // Boundary case check
        if (mins == 60) {
            hrs += 1;
            mins = 0;
        }

        if (hrs == 0) {
            return mins + " mins";
        } else if (mins == 0) {
            return hrs + " hrs";
        }
        return String.format("%d hrs %d mins", hrs, mins);
    }

    private static class Coordinate {
        double lat;
        double lon;

        Coordinate(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }
    }
}
