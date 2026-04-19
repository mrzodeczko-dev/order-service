package com.rzodeczko.infrastructure.adapter.http.dto;

import java.math.BigDecimal;

public record ItemRequestDto(String name, int quantity, BigDecimal price) {
}
