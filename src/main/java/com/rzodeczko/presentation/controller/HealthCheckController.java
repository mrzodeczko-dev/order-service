package com.rzodeczko.presentation.controller;


import com.rzodeczko.configuration.properties.IntegrationProperties;
import com.rzodeczko.presentation.dto.HealthCheckResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {
    private final IntegrationProperties integrationProperties;

    @GetMapping("/")
    public ResponseEntity<HealthCheckResponseDto> healthCheck() {
        log.info("PAYMENT: {}", integrationProperties.payment().url());
        log.info("INVOICE: {}", integrationProperties.invoice().url());
        return ResponseEntity.ok(new HealthCheckResponseDto("ORDER SERVICE OK"));
    }
}
