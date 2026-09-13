package com.sravanthi.logAnalytics.controller;

import com.sravanthi.logAnalytics.service.AthenaQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/athena")
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