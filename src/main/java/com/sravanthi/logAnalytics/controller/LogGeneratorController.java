package com.sravanthi.logAnalytics.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

//The LogGeneratorController simulates different application events to generate INFO, WARN and ERROR
// logs for demonstrating the complete analytics pipeline.
@RestController
@RequestMapping("/api/logs")
public class LogGeneratorController {

    private static final Logger log =
            LoggerFactory.getLogger(LogGeneratorController.class);

    @GetMapping("/generate")
    public ResponseEntity<Map<String, Object>> processOrder(
            @RequestParam String orderId,
            @RequestParam double amount) {

        String correlationId = UUID.randomUUID().toString();

        try {
            MDC.put("correlationId", correlationId);
            MDC.put("orderId", orderId);

            log.info("Order processing started. Amount={}", amount);

            if (amount <= 0) {
                log.error("Order processing failed because amount is invalid. Amount={}",
                        amount);

                return ResponseEntity.badRequest().body(Map.of(
                        "status", "FAILED",
                        "message", "Amount must be greater than zero",
                        "orderId", orderId,
                        "correlationId", correlationId
                ));
            }

            if (amount >= 5000) {
                log.warn("High-value order detected. Amount={}", amount);
            }

            log.info("Order processed successfully. Amount={}", amount);

            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "orderId", orderId,
                    "amount", amount,
                    "correlationId", correlationId
            ));
        } finally {
            MDC.clear();
        }
    }
}
