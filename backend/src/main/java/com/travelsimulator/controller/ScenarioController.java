package com.travelsimulator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelsimulator.dto.*;
import com.travelsimulator.entity.Scenario;
import com.travelsimulator.entity.ScenarioResult;
import com.travelsimulator.entity.TripScenarioSession;
import com.travelsimulator.entity.User;
import com.travelsimulator.repository.ScenarioRepository;
import com.travelsimulator.repository.ScenarioResultRepository;
import com.travelsimulator.repository.TripScenarioSessionRepository;
import com.travelsimulator.repository.UserRepository;
import com.travelsimulator.security.UserPrincipal;
import com.travelsimulator.service.AiReasoningService;
import com.travelsimulator.service.DecisionScoreEngine;
import com.travelsimulator.service.DistanceService;
import com.travelsimulator.service.PdfExportService;
import com.travelsimulator.service.WeatherService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioController {

    private static final Logger logger = LoggerFactory.getLogger(ScenarioController.class);

    private final TripScenarioSessionRepository sessionRepository;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioResultRepository resultRepository;
    private final UserRepository userRepository;
    private final WeatherService weatherService;
    private final DistanceService distanceService;
    private final AiReasoningService aiReasoningService;
    private final DecisionScoreEngine scoreEngine;
    private final PdfExportService pdfExportService;
    private final ObjectMapper objectMapper;

    public ScenarioController(TripScenarioSessionRepository sessionRepository,
                              ScenarioRepository scenarioRepository,
                              ScenarioResultRepository resultRepository,
                              UserRepository userRepository,
                              WeatherService weatherService,
                              DistanceService distanceService,
                              AiReasoningService aiReasoningService,
                              DecisionScoreEngine scoreEngine,
                              PdfExportService pdfExportService,
                              ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.scenarioRepository = scenarioRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.weatherService = weatherService;
        this.distanceService = distanceService;
        this.aiReasoningService = aiReasoningService;
        this.scoreEngine = scoreEngine;
        this.pdfExportService = pdfExportService;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a base trip scenario session + what-if variations
     */
    @PostMapping("/session")
    @Transactional
    public ResponseEntity<ApiResponse<ScenarioSessionResponse>> createSession(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ScenarioSessionRequest request) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        }

        try {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Serialize base trip request to store in session
            String baseTripJson = objectMapper.writeValueAsString(request);
            
            TripScenarioSession session = new TripScenarioSession(user, baseTripJson);
            
            // 1. Create Base Scenario
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            Scenario baseScenario = new Scenario(
                    session,
                    "Base Plan: " + request.getDestination(),
                    request.getDestination(),
                    startDate,
                    request.getBudget(),
                    request.getGroupType(),
                    request.getTravelMode(),
                    true // isBaseScenario
            );
            session.addScenario(baseScenario);

            // 2. Parse and Create What-If Scenarios
            if (request.getWhatIfs() != null) {
                for (String whatIfText : request.getWhatIfs()) {
                    if (whatIfText == null || whatIfText.trim().isEmpty()) continue;

                    // Copy values from base trip initially
                    Scenario whatIfScenario = new Scenario(
                            session,
                            whatIfText.trim(),
                            request.getDestination(),
                            startDate,
                            request.getBudget(),
                            request.getGroupType(),
                            request.getTravelMode(),
                            false // isBaseScenario
                    );

                    applyWhatIfModifications(whatIfScenario, whatIfText);
                    session.addScenario(whatIfScenario);
                }
            }

            TripScenarioSession savedSession = sessionRepository.save(session);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Trip simulation session created", 
                    mapToSessionResponse(savedSession)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error creating session: " + e.getMessage()));
        }
    }

    /**
     * Get session scenarios + result list
     */
    @GetMapping("/session/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ScenarioSessionResponse>> getSessionDetails(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        }

        Optional<TripScenarioSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty() || !sessionOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Session not found"));
        }

        return ResponseEntity.ok(ApiResponse.success("Session details retrieved", mapToSessionResponse(sessionOpt.get())));
    }

    /**
     * Trigger weather + AI + scoring pipeline for a single scenario
     */
    @PostMapping("/{id}/compute")
    @Transactional
    public ResponseEntity<ApiResponse<ScenarioResponse>> computeScenario(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        }

        Optional<Scenario> scenarioOpt = scenarioRepository.findById(id);
        if (scenarioOpt.isEmpty() || !scenarioOpt.get().getSession().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Scenario not found"));
        }

        Scenario scenario = scenarioOpt.get();

        try {
            int month = scenario.getStartDate().getMonthValue();
            String datesStr = scenario.getStartDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

            // 1. Weather service
            WeatherResponse weather = weatherService.getWeatherData(scenario.getDestination(), month);

            // 2. Distance service
            DistanceResponse distance = distanceService.estimateDistanceAndDuration(
                    "Mumbai", // Assumes Mumbai as standard base hub for calculation
                    scenario.getDestination(),
                    scenario.getTravelMode()
            );

            // 3. AI Reasoning service (throws AiServiceException if API key fails)
            String weatherText = String.format("Temperature: %.1fC, conditions: %s", weather.getTemp(), weather.getDescription());
            AiReasoningResponse aiResult = aiReasoningService.getAiReasoning(
                    scenario.getDestination(),
                    datesStr,
                    scenario.getBudget(),
                    scenario.getGroupType(),
                    scenario.getTravelMode(),
                    weatherText
            );

            // 4. Decision score engine
            DecisionScoreBreakdown breakdown = scoreEngine.calculateDecisionScore(scenario, weather, distance, aiResult);

            // 5. Store / Update results
            ScenarioResult result = scenario.getResult();
            if (result == null) {
                result = new ScenarioResult(scenario);
            }

            result.setBudgetProjection(scenario.getBudget()); // baseline projection
            result.setHiddenExpenses(aiResult.getHiddenExpenseEstimate());
            result.setWeatherSummary(weather.getDescription() + " (" + Math.round(weather.getTemp()) + "°C)");
            result.setCrowdLevel(aiResult.getCrowdLevelEstimate());
            result.setSafetyScore(breakdown.getSafety().intValue());
            result.setTravelTimeEstimate(distance.getTravelTimeEstimate());
            result.setItineraryJson(objectMapper.writeValueAsString(aiResult.getItinerary()));
            result.setDecisionScore(breakdown.getFinalScore());
            result.setDecisionBreakdownJson(objectMapper.writeValueAsString(breakdown));
            result.setAiExplanation(aiResult.getReasoningExplanation());

            scenario.setResult(result);
            Scenario savedScenario = scenarioRepository.save(scenario);

            return ResponseEntity.ok(ApiResponse.success(
                    "Scenario calculations computed successfully", 
                    mapToScenarioResponse(savedScenario)
            ));

        } catch (Exception e) {
            // Guardrail Section 6 check: ensure the exception response shows the error to the UI
            logger.error("Error computing pipeline: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Pipeline failure: " + e.getMessage()));
        }
    }

    /**
     * List saved sessions for the logged in user
     */
    @GetMapping("/history")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<ScenarioSessionResponse>>> getHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
        }

        List<TripScenarioSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        List<ScenarioSessionResponse> responses = new ArrayList<>();
        for (TripScenarioSession session : sessions) {
            responses.add(mapToSessionResponse(session));
        }

        return ResponseEntity.ok(ApiResponse.success("Session history retrieved", responses));
    }

    /**
     * Export comparison PDF report
     */
    @GetMapping("/session/{id}/export")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<TripScenarioSession> sessionOpt = sessionRepository.findById(id);
        if (sessionOpt.isEmpty() || !sessionOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        byte[] pdfBytes = pdfExportService.generateComparisonPdf(sessionOpt.get());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "comparison_report_" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Apply modifications to the scenario parameters based on natural language what-if text.
     */
    private void applyWhatIfModifications(Scenario scenario, String whatIf) {
        String query = whatIf.toLowerCase();

        // 1. Season/Date tweaks
        if (query.contains("rainy") || query.contains("monsoon")) {
            scenario.setStartDate(LocalDate.of(scenario.getStartDate().getYear(), 7, 15)); // July
        } else if (query.contains("winter") || query.contains("snow") || query.contains("december")) {
            scenario.setStartDate(LocalDate.of(scenario.getStartDate().getYear(), 12, 15)); // December
        } else if (query.contains("summer") || query.contains("hot") || query.contains("may")) {
            scenario.setStartDate(LocalDate.of(scenario.getStartDate().getYear(), 5, 15)); // May
        } else if (query.contains("next month")) {
            scenario.setStartDate(scenario.getStartDate().plusMonths(1));
        }

        // 2. Group type tweaks
        if (query.contains("family")) {
            scenario.setGroupType("family");
        } else if (query.contains("solo")) {
            scenario.setGroupType("solo");
        } else if (query.contains("friends")) {
            scenario.setGroupType("friends");
        } else if (query.contains("couple")) {
            scenario.setGroupType("couple");
        }

        // 3. Travel mode tweaks
        if (query.contains("train")) {
            scenario.setTravelMode("train");
        } else if (query.contains("flight") || query.contains("fly")) {
            scenario.setTravelMode("flight");
        } else if (query.contains("car") || query.contains("road") || query.contains("drive")) {
            scenario.setTravelMode("car");
        }

        // 4. Budget tweaks (e.g. "budget 25000" or "budget to 45000")
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\b(\\d{4,})\\b").matcher(query);
            double lastNum = -1;
            while (m.find()) {
                lastNum = Double.parseDouble(m.group(1));
            }
            if (lastNum > 0) {
                scenario.setBudget(lastNum);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    private ScenarioSessionResponse mapToSessionResponse(TripScenarioSession session) {
        List<ScenarioResponse> scenarioResponses = new ArrayList<>();
        for (Scenario scenario : session.getScenarios()) {
            scenarioResponses.add(mapToScenarioResponse(scenario));
        }

        return new ScenarioSessionResponse(
                session.getId(),
                session.getBaseTripJson(),
                session.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                scenarioResponses
        );
    }

    private ScenarioResponse mapToScenarioResponse(Scenario scenario) {
        ScenarioResponse resp = new ScenarioResponse();
        resp.setId(scenario.getId());
        resp.setLabel(scenario.getLabel());
        resp.setDestination(scenario.getDestination());
        resp.setStartDate(scenario.getStartDate().toString());
        resp.setBudget(scenario.getBudget());
        resp.setGroupType(scenario.getGroupType());
        resp.setTravelMode(scenario.getTravelMode());
        resp.setBaseScenario(scenario.isBaseScenario());

        ScenarioResult res = scenario.getResult();
        if (res != null) {
            resp.setHasResult(true);
            resp.setBudgetProjection(res.getBudgetProjection());
            resp.setHiddenExpenses(res.getHiddenExpenses());
            resp.setWeatherSummary(res.getWeatherSummary());
            resp.setCrowdLevel(res.getCrowdLevel());
            resp.setSafetyScore(res.getSafetyScore());
            resp.setTravelTimeEstimate(res.getTravelTimeEstimate());
            resp.setItineraryJson(res.getItineraryJson());
            resp.setDecisionScore(res.getDecisionScore());
            resp.setDecisionBreakdownJson(res.getDecisionBreakdownJson());
            resp.setAiExplanation(res.getAiExplanation());
        } else {
            resp.setHasResult(false);
        }

        return resp;
    }
}
