package com.sravanthi.logAnalytics.controller;

import com.sravanthi.logAnalytics.service.AthenaQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/athena")
@CrossOrigin(origins = "http://localhost:5173")
public class AthenaController {

    private final AthenaQueryService athenaQueryService;

    public AthenaController(AthenaQueryService athenaQueryService) {
        this.athenaQueryService = athenaQueryService;
    }

    @GetMapping("/logs")
    public List<Map<String, String>> findLogs(
            @RequestParam String year,
            @RequestParam String month,
            @RequestParam String day,
            @RequestParam String orderId) {

        return athenaQueryService.findLogs(
                year,
                month,
                day,
                orderId);
    }
}