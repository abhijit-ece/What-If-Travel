package com.travelsimulator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsimulator.dto.AiReasoningResponse;
import com.travelsimulator.dto.WeatherResponse;
import com.travelsimulator.entity.Scenario;
import com.travelsimulator.entity.TripScenarioSession;
import com.travelsimulator.entity.User;
import com.travelsimulator.repository.ScenarioRepository;
import com.travelsimulator.repository.TripScenarioSessionRepository;
import com.travelsimulator.repository.UserRepository;
import com.travelsimulator.security.CustomUserDetailsService;
import com.travelsimulator.security.UserPrincipal;
import com.travelsimulator.service.AiReasoningService;
import com.travelsimulator.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ScenarioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripScenarioSessionRepository sessionRepository;

    @Autowired
    private ScenarioRepository scenarioRepository;

    @MockBean
    private WeatherService weatherService;

    @MockBean
    private AiReasoningService aiReasoningService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private Scenario testScenario;

    @BeforeEach
    public void setup() {
        // Clear H2 DB for fresh run
        scenarioRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create and save a test User
        testUser = new User("Test Traveler", "test@example.com", "hashedpassword");
        testUser = userRepository.save(testUser);

        // 2. Mock Security Authentication Context
        UserDetails userDetails = userDetailsService.loadUserById(testUser.getId());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 3. Create and save a Session + Scenario
        TripScenarioSession session = new TripScenarioSession(testUser, "{}");
        testScenario = new Scenario(
                session, "Base Trip", "Goa", 
                LocalDate.of(2026, 12, 15), 25000.0, "solo", "flight", true
        );
        session.addScenario(testScenario);
        session = sessionRepository.save(session);
        testScenario = session.getScenarios().get(0);
    }

    @Test
    public void testComputePipelineEndpointSuccess() throws Exception {
        // Mock Weather Response
        WeatherResponse mockWeather = new WeatherResponse(25.0, 25.0, 60, "clear sky", 4.0, true);
        Mockito.when(weatherService.getWeatherData(anyString(), anyInt())).thenReturn(mockWeather);

        // Mock AI Reasoning Response
        AiReasoningResponse mockAi = new AiReasoningResponse(
                45, "Safe", 1500.0, 
                Collections.emptyList(), Collections.emptyList(), "Highly recommended"
        );
        Mockito.when(aiReasoningService.getAiReasoning(
                anyString(), anyString(), anyDouble(), anyString(), anyString(), anyString())
        ).thenReturn(mockAi);

        // Call compute endpoint using MockMvc
        mockMvc.perform(post("/api/scenarios/" + testScenario.getId() + "/compute")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.hasResult").value(true))
                .andExpect(jsonPath("$.data.decisionScore").isNumber())
                .andExpect(jsonPath("$.data.weatherSummary").value("clear sky (25°C)"))
                .andExpect(jsonPath("$.data.hiddenExpenses").value(1500.0))
                .andExpect(jsonPath("$.data.aiExplanation").value("Highly recommended"));
    }
}
