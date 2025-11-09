package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.model.*;
import com.example.demo.service.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/errors")
@CrossOrigin(origins = "*")
public class ErrorController {

    private final ErrorService errorService;
    private final ErrorSearchService errorSearchService;
    private final ErrorAnalyticsService errorAnalyticsService;
    private final ErrorChatService errorChatService;
    private final ErrorGroupingService errorGroupingService;

    public ErrorController(
            ErrorService errorService,
            ErrorSearchService errorSearchService,
            ErrorAnalyticsService errorAnalyticsService,
            ErrorChatService errorChatService,
            ErrorGroupingService errorGroupingService) {
        this.errorService = errorService;
        this.errorSearchService = errorSearchService;
        this.errorAnalyticsService = errorAnalyticsService;
        this.errorChatService = errorChatService;
        this.errorGroupingService = errorGroupingService;
    }

    // ========== Error Submission ==========

    @PostMapping
    public ResponseEntity<Map<String, Object>> submitError(
            @RequestBody ErrorSubmissionRequest request) {
        try {
            ErrorLog errorLog = errorService.ingestError(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("errorId", errorLog.getId());
            response.put("message", "Error logged successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to log error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Error Retrieval ==========

    @GetMapping
    public ResponseEntity<List<ErrorLog>> getAllErrors() {
        List<ErrorLog> errors = errorService.getAllErrors();
        return ResponseEntity.ok(errors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ErrorLog> getError(@PathVariable Long id) {
        return errorService
                .getErrorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ErrorLog>> getRecentErrors(
            @RequestParam(defaultValue = "24") int hours) {
        List<ErrorLog> errors = errorService.getRecentErrors(hours);
        return ResponseEntity.ok(errors);
    }

    // ========== Semantic Search ==========

    @PostMapping("/search")
    public ResponseEntity<List<SimilarError>> searchErrors(
            @RequestBody Map<String, Object> searchRequest) {
        String query = (String) searchRequest.get("query");
        Integer topK = (Integer) searchRequest.getOrDefault("topK", 10);
        Double threshold = (Double) searchRequest.getOrDefault("threshold", 0.7);

        List<SimilarError> results = errorSearchService.findSimilarErrors(query, topK, threshold);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<List<SimilarError>> findSimilarErrors(
            @PathVariable Long id, @RequestParam(defaultValue = "5") int topK) {
        try {
            ErrorLog errorLog =
                    errorService
                            .getErrorById(id)
                            .orElseThrow(() -> new RuntimeException("Error not found"));
            // Request one more than topK to account for filtering out the current error
            List<SimilarError> similar =
                    errorSearchService.findSimilarErrors(errorLog.getStackTrace(), topK + 1, 0.7);
            // Filter out the current error and limit to topK
            List<SimilarError> filtered =
                    similar.stream()
                            .filter(s -> !s.getErrorLog().getId().equals(id))
                            .limit(topK)
                            .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(filtered);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== Analytics ==========

    @GetMapping("/analytics/trends")
    public ResponseEntity<Map<String, Object>> getErrorTrends(
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> trends = errorAnalyticsService.getErrorTrends(hours);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/analytics/trends/daily")
    public ResponseEntity<Map<String, Object>> getDailyTrends(
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> trends = errorAnalyticsService.getErrorTrendsByDay(days);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/analytics/spikes")
    public ResponseEntity<Map<String, Object>> detectSpikes(
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> spikes = errorAnalyticsService.detectSpikes(hours);
        return ResponseEntity.ok(spikes);
    }

    @GetMapping("/analytics/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> stats = errorAnalyticsService.getStatistics(hours);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<List<Map<String, Object>>> getRecentSummary(
            @RequestParam(defaultValue = "50") int limit) {
        List<Map<String, Object>> summary = errorAnalyticsService.getRecentErrorsSummary(limit);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/analytics/compare")
    public ResponseEntity<Map<String, Object>> compareTimePeriods(
            @RequestParam(defaultValue = "24") int currentHours,
            @RequestParam(defaultValue = "24") int previousHours) {
        Map<String, Object> comparison =
                errorAnalyticsService.compareTimePeriods(currentHours, previousHours);
        return ResponseEntity.ok(comparison);
    }

    // ========== AI Chat ==========

    @PostMapping("/chat/ask")
    public ResponseEntity<Map<String, String>> askQuestion(
            @RequestBody Map<String, String> request) {
        String question = request.get("question");
        String answer = errorChatService.askQuestion(question);

        Map<String, String> response = new HashMap<>();
        response.put("question", question);
        response.put("answer", answer);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/ask-about/{id}")
    public ResponseEntity<Map<String, String>> askAboutError(
            @PathVariable Long id, @RequestBody Map<String, String> request) {
        String question = request.get("question");
        String answer = errorChatService.askAboutError(id, question);

        Map<String, String> response = new HashMap<>();
        response.put("errorId", id.toString());
        response.put("question", question);
        response.put("answer", answer);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat/insights")
    public ResponseEntity<Map<String, String>> getInsights(
            @RequestParam String applicationName, @RequestParam String environment) {
        String insights = errorChatService.getErrorInsights(applicationName, environment);

        Map<String, String> response = new HashMap<>();
        response.put("applicationName", applicationName);
        response.put("environment", environment);
        response.put("insights", insights);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chat/compare/{id1}/{id2}")
    public ResponseEntity<Map<String, String>> compareErrors(
            @PathVariable Long id1, @PathVariable Long id2) {
        String comparison = errorChatService.compareErrors(id1, id2);

        Map<String, String> response = new HashMap<>();
        response.put("error1Id", id1.toString());
        response.put("error2Id", id2.toString());
        response.put("comparison", comparison);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chat/suggest-solution")
    public ResponseEntity<Map<String, String>> suggestSolution(
            @RequestBody Map<String, String> request) {
        String errorDescription = request.get("errorDescription");
        String solution = errorChatService.suggestSolutions(errorDescription);

        Map<String, String> response = new HashMap<>();
        response.put("errorDescription", errorDescription);
        response.put("solution", solution);
        return ResponseEntity.ok(response);
    }

    // ========== Trend Reports ==========

    @GetMapping("/reports/trend")
    public ResponseEntity<Map<String, Object>> generateTrendReport(
            @RequestParam(defaultValue = "all") String applicationName,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "standard") String reportType) {

        String report = errorChatService.generateTrendReport(applicationName, days, reportType);

        Map<String, Object> response = new HashMap<>();
        response.put("applicationName", applicationName);
        response.put("days", days);
        response.put("reportType", reportType);
        response.put("report", report);
        response.put("generatedAt", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // ========== Root Cause Analysis ==========

    @PostMapping("/analytics/root-cause/{errorId}")
    public ResponseEntity<Map<String, Object>> analyzeRootCause(@PathVariable Long errorId) {
        try {
            String analysis = errorChatService.analyzeRootCause(errorId);

            Map<String, Object> response = new HashMap<>();
            response.put("errorId", errorId);
            response.put("analysis", analysis);
            response.put("generatedAt", java.time.LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to analyze root cause: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/analytics/spike-detection/{errorId}")
    public ResponseEntity<Map<String, Object>> detectSpike(@PathVariable Long errorId) {
        try {
            Map<String, Object> spikeInfo = errorAnalyticsService.detectErrorSpike(errorId);
            return ResponseEntity.ok(spikeInfo);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to detect spike: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/analytics/correlated-errors/{errorId}")
    public ResponseEntity<List<ErrorLog>> getCorrelatedErrors(@PathVariable Long errorId) {
        try {
            List<ErrorLog> correlatedErrors = errorAnalyticsService.findCorrelatedErrors(errorId);
            return ResponseEntity.ok(correlatedErrors);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== Intelligent Error Grouping ==========

    @GetMapping("/groups")
    public ResponseEntity<List<ErrorGroup>> getErrorGroups(
            @RequestParam(defaultValue = "0.75") double similarityThreshold,
            @RequestParam(defaultValue = "2") int minGroupSize,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<ErrorGroup> groups =
                    errorGroupingService.groupErrors(similarityThreshold, minGroupSize, hours);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/groups/all")
    public ResponseEntity<List<ErrorGroup>> getAllErrorGroups(
            @RequestParam(defaultValue = "0.75") double similarityThreshold,
            @RequestParam(defaultValue = "2") int minGroupSize) {
        try {
            List<ErrorGroup> groups =
                    errorGroupingService.groupAllErrors(similarityThreshold, minGroupSize);
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/groups/statistics")
    public ResponseEntity<Map<String, Object>> getGroupingStatistics(
            @RequestParam(defaultValue = "0.75") double similarityThreshold,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Map<String, Object> stats =
                    errorGroupingService.getGroupingStatistics(similarityThreshold, hours);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ========== Testing Endpoints ==========

    @PostMapping("/test/random")
    public ResponseEntity<Map<String, Object>> generateRandomError() {
        try {
            ErrorLog errorLog = errorService.generateRandomTestError();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("errorId", errorLog.getId());
            response.put("occurredAt", errorLog.getOccurredAt().toString());
            response.put("message", "Random test error generated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ========== Health Check ==========

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Error Analysis System");
        return ResponseEntity.ok(health);
    }
}
