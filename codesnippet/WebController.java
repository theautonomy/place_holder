package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import com.example.demo.model.ErrorGroup;
import com.example.demo.model.ErrorLog;
import com.example.demo.service.ErrorAnalyticsService;
import com.example.demo.service.ErrorGroupingService;
import com.example.demo.service.ErrorService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {

    private final ErrorService errorService;
    private final ErrorAnalyticsService errorAnalyticsService;
    private final ErrorGroupingService errorGroupingService;

    public WebController(
            ErrorService errorService,
            ErrorAnalyticsService errorAnalyticsService,
            ErrorGroupingService errorGroupingService) {
        this.errorService = errorService;
        this.errorAnalyticsService = errorAnalyticsService;
        this.errorGroupingService = errorGroupingService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(defaultValue = "all") String applicationName,
            @RequestParam(defaultValue = "24") int hours,
            @RequestParam(defaultValue = "5") int limit,
            @RequestParam(defaultValue = "10") int recentLimit,
            Model model) {

        // Get statistics
        Map<String, Object> stats =
                errorAnalyticsService.getErrorStatistics(applicationName, hours, limit);
        model.addAttribute("stats", stats);
        model.addAttribute("applicationName", applicationName);
        model.addAttribute("hours", hours);
        model.addAttribute("limit", limit);
        model.addAttribute("recentLimit", recentLimit);

        // Get recent errors
        List<ErrorLog> recentErrors = errorService.getRecentErrors(hours, recentLimit);
        model.addAttribute("recentErrors", recentErrors);

        return "dashboard";
    }

    @GetMapping("/errors")
    public String errorList(@RequestParam(defaultValue = "24") int hours, Model model) {
        List<ErrorLog> errors = errorService.getRecentErrors(hours);
        model.addAttribute("errors", errors);
        return "error-list";
    }

    @GetMapping("/errors/{id}")
    public String errorDetail(@PathVariable Long id, Model model) {
        return errorService
                .getErrorById(id)
                .map(
                        error -> {
                            model.addAttribute("error", error);
                            // Get occurrence statistics
                            Map<String, Object> stats = errorService.getErrorOccurrenceStats(id);
                            model.addAttribute("occurrenceStats", stats);
                            return "error-detail";
                        })
                .orElse("redirect:/errors");
    }

    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

    @GetMapping("/chat")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/analytics")
    public String analyticsPage(
            @RequestParam(defaultValue = "all") String applicationName,
            @RequestParam(defaultValue = "7") int days,
            Model model) {

        Map<String, Object> trends = errorAnalyticsService.detectTrends(applicationName, days);
        Map<String, Object> comparison = errorAnalyticsService.compareEnvironments(applicationName);

        model.addAttribute("trends", trends);
        model.addAttribute("comparison", comparison);
        model.addAttribute("applicationName", applicationName);
        model.addAttribute("days", days);

        return "analytics";
    }

    @GetMapping("/reports")
    public String reportsPage(
            @RequestParam(defaultValue = "all") String applicationName,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "standard") String reportType,
            Model model) {

        model.addAttribute("applicationName", applicationName);
        model.addAttribute("days", days);
        model.addAttribute("reportType", reportType);

        return "trend-report";
    }

    @GetMapping("/groups")
    public String errorGroupsPage(
            @RequestParam(defaultValue = "0.75") double similarityThreshold,
            @RequestParam(defaultValue = "2") int minGroupSize,
            @RequestParam(defaultValue = "24") int hours,
            Model model) {

        List<ErrorGroup> groups =
                errorGroupingService.groupErrors(similarityThreshold, minGroupSize, hours);
        Map<String, Object> stats =
                errorGroupingService.getGroupingStatistics(similarityThreshold, hours);

        model.addAttribute("groups", groups);
        model.addAttribute("stats", stats);
        model.addAttribute("similarityThreshold", similarityThreshold);
        model.addAttribute("minGroupSize", minGroupSize);
        model.addAttribute("hours", hours);

        return "error-groups";
    }
}
