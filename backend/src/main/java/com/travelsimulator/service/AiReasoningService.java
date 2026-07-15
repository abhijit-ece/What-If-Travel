package com.travelsimulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsimulator.dto.AiReasoningResponse;
import com.travelsimulator.exception.AiServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiReasoningService {

    private static final Logger logger = LoggerFactory.getLogger(AiReasoningService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String provider;

    public AiReasoningService(RestTemplate restTemplate,
                              ObjectMapper objectMapper,
                              @Value("${travelsimulator.ai.api-key:}") String apiKey,
                              @Value("${travelsimulator.ai.provider:gemini}") String provider) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.provider = provider;
    }

    /**
     * Sends a request to the configured LLM provider and parses the result into an AiReasoningResponse.
     */
    public AiReasoningResponse getAiReasoning(String destination, String dates, Double budget, 
                                              String groupType, String travelMode, String weatherContext) {
        
        if ("mock".equalsIgnoreCase(provider)) {
            return generateMockResponse(destination, dates, budget, groupType, travelMode, weatherContext);
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.startsWith("your_")) {
            logger.error("AI API Key is missing or not configured. Cannot perform simulation.");
            throw new AiServiceException("Live AI service is not configured. Please set a valid LLM_API_KEY in the environment.");
        }

        String prompt = buildSystemPrompt(destination, dates, budget, groupType, travelMode, weatherContext);

        if ("gemini".equalsIgnoreCase(provider)) {
            return callGemini(prompt);
        } else {
            throw new AiServiceException("Unsupported AI provider configured: " + provider);
        }
    }

    private AiReasoningResponse generateMockResponse(String destination, String dates, Double budget, 
                                                    String groupType, String travelMode, String weatherContext) {
        AiReasoningResponse response = new AiReasoningResponse();
        
        int crowd = 50;
        if (weatherContext.toLowerCase().contains("rain") || weatherContext.toLowerCase().contains("monsoon")) {
            crowd = 30;
        } else if (dates.toLowerCase().contains("december") || dates.toLowerCase().contains("january")) {
            crowd = 85;
        }
        response.setCrowdLevelEstimate(crowd);

        double hidden = 1000.0 + (budget * 0.05);
        if ("flight".equalsIgnoreCase(travelMode)) {
            hidden += 1500.0;
        }
        response.setHiddenExpenseEstimate(hidden);

        String safety = "Standard travel safety precautions apply. Ensure your belongings are secure.";
        if (weatherContext.toLowerCase().contains("monsoon") || weatherContext.toLowerCase().contains("rain")) {
            safety = "Seasonal monsoon warnings active. Expect beach swimming restrictions and potential road waterlogging.";
        } else if (weatherContext.toLowerCase().contains("snow") || weatherContext.toLowerCase().contains("storm")) {
            safety = "Severe winter weather hazard check. High altitude terrain roads may be closed due to blizzards.";
        }
        response.setSafetyNotes(safety);

        response.setReasoningExplanation(String.format(
                "Offline simulation results for %s in %s. Budget of INR %.0f is suitable. Safety index is adapted for %s hazards.",
                destination, dates, budget, travelMode
        ));

        response.setAlternativeLocations(Arrays.asList("Gokarna", "Matheran"));

        List<AiReasoningResponse.ItineraryDay> itinerary = new ArrayList<>();
        
        AiReasoningResponse.ItineraryDay day1 = new AiReasoningResponse.ItineraryDay();
        day1.setDay(1);
        day1.setTitle("Arrival & Sightseeing");
        day1.setActivities(Arrays.asList("Check-in at accommodation", "Explore local markets", "Dinner at local diner"));
        itinerary.add(day1);

        AiReasoningResponse.ItineraryDay day2 = new AiReasoningResponse.ItineraryDay();
        day2.setDay(2);
        day2.setTitle("Adventure & Activity");
        day2.setActivities(Arrays.asList("Visit primary cultural landmark", "Outdoor activity scaled for group " + groupType));
        itinerary.add(day2);

        response.setItinerary(itinerary);

        return response;
    }

    private String buildSystemPrompt(String destination, String dates, Double budget, 
                                     String groupType, String travelMode, String weatherContext) {
        return String.format("""
            You are an expert AI Travel Analyst. Your job is to simulate and analyze a travel scenario and output a structured JSON response.
            Analyze safety risk, seasonal crowd factors, hidden cost risks (local transport surcharge, entry permits, monsoon season closures), day-by-day plan tailored to the group type, and alternative destinations.
            
            Do NOT include any markdown code blocks, preambles, or postambles. Output ONLY raw JSON matching this schema exactly:
            {
              "crowdLevelEstimate": 45,
              "safetyNotes": "Details about safety constraints or alerts (monsoon landslides, local scams, etc.)",
              "hiddenExpenseEstimate": 3500.00,
              "itinerary": [
                {
                  "day": 1,
                  "title": "Day Title",
                  "activities": ["Activity 1", "Activity 2"]
                }
              ],
              "alternativeLocations": ["Alternative Destination 1", "Alternative Destination 2"],
              "reasoningExplanation": "Plain explanation explaining the details of this scenario and why it is rated the way it is."
            }

            Travel Scenario Details:
            - Destination: %s
            - Travel Dates / Season: %s
            - Stated Budget: INR %s
            - Group Composition: %s
            - Travel Mode: %s
            - Weather Context: %s
            """, destination, dates, budget, groupType, travelMode, weatherContext);
    }

    @SuppressWarnings("unchecked")
    private AiReasoningResponse callGemini(String prompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey.trim();

            logger.info("Posting request to Gemini API...");

            // Create contents object matching Gemini structure
            Map<String, Object> textPart = Map.of("text", prompt);
            Map<String, Object> partContainer = Map.of("parts", List.of(textPart));
            // Gemini expects "role": "user" or equivalent
            Map<String, Object> contentContainer = Map.of("role", "user", "parts", List.of(textPart));
            
            Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "responseMimeType", "application/json"
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(contentContainer));
            requestBody.put("generationConfig", generationConfig);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List candidates = (List) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map candidate = (Map) candidates.get(0);
                    Map content = (Map) candidate.get("content");
                    List parts = (List) content.get("parts");
                    Map part = (Map) parts.get(0);
                    String rawJson = (String) part.get("text");

                    logger.debug("Raw Gemini JSON response: {}", rawJson);
                    
                    // Parse JSON using ObjectMapper
                    return objectMapper.readValue(rawJson, AiReasoningResponse.class);
                }
            }
            
            throw new AiServiceException("Failed to fetch response. Received empty payload from Gemini API.");
        } catch (Exception e) {
            logger.error("Failed to execute or parse AI reasoning call: {}", e.getMessage());
            throw new AiServiceException("Could not fetch live AI reasoning: " + e.getMessage(), e);
        }
    }
}
