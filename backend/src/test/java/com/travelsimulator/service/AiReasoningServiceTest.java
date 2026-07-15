package com.travelsimulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsimulator.dto.AiReasoningResponse;
import com.travelsimulator.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class AiReasoningServiceTest {

    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private AiReasoningService aiReasoningService;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        restTemplate = Mockito.mock(RestTemplate.class);
        // Set API key to valid mock to avoid immediate key checks
        aiReasoningService = new AiReasoningService(restTemplate, objectMapper, "mock-api-key", "gemini");
    }

    @Test
    public void testValidJsonParsing() throws Exception {
        String validJson = """
            {
              "crowdLevelEstimate": 60,
              "safetyNotes": "Watch for rip currents.",
              "hiddenExpenseEstimate": 2500.0,
              "itinerary": [
                {
                  "day": 1,
                  "title": "Beach Day",
                  "activities": ["Chill at Baga Beach", "Sunset dinner"]
                }
              ],
              "alternativeLocations": ["Gokarna", "Alibaug"],
              "reasoningExplanation": "Highly recommended off-season trip."
            }
            """;

        // Parse using Jackson ObjectMapper to verify mapping
        AiReasoningResponse response = objectMapper.readValue(validJson, AiReasoningResponse.class);

        assertNotNull(response);
        assertEquals(60, response.getCrowdLevelEstimate());
        assertEquals("Watch for rip currents.", response.getSafetyNotes());
        assertEquals(2500.0, response.getHiddenExpenseEstimate());
        assertEquals(1, response.getItinerary().size());
        assertEquals("Beach Day", response.getItinerary().get(0).getTitle());
        assertEquals(2, response.getItinerary().get(0).getActivities().size());
        assertEquals("Chill at Baga Beach", response.getItinerary().get(0).getActivities().get(0));
        assertEquals(2, response.getAlternativeLocations().size());
        assertEquals("Gokarna", response.getAlternativeLocations().get(0));
        assertEquals("Highly recommended off-season trip.", response.getReasoningExplanation());
    }

    @Test
    public void testMalformedJsonParsingThrowsException() {
        // Missing brackets and quotes
        String malformedJson = """
            {
              "crowdLevelEstimate": 60
              "safetyNotes": Watch for rip currents.,
              "hiddenExpenseEstimate": 2500.0,
            }
            """;

        // Deserializing this should throw a Jackson exception
        assertThrows(Exception.class, () -> {
            objectMapper.readValue(malformedJson, AiReasoningResponse.class);
        });
    }

    @Test
    public void testMissingApiKeyThrowsAiServiceException() {
        // Create service with empty API key
        AiReasoningService invalidService = new AiReasoningService(restTemplate, objectMapper, "", "gemini");

        // Calling reasoning should throw AiServiceException due to missing key
        assertThrows(AiServiceException.class, () -> {
            invalidService.getAiReasoning("Goa", "July 2026", 15000.0, "solo", "flight", "Sunny");
        });
    }
}
