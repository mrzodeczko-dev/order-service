package com.rzodeczko.presentation.controller;

import com.rzodeczko.presentation.dto.response.HealthCheckResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthCheckController {

    @GetMapping("/")
    public ResponseEntity<HealthCheckResponseDto> healthCheck() {
        return ResponseEntity.ok(new HealthCheckResponseDto("ORDER SERVICE OK"));
    }
}
