package com.rzodeczko.infrastructure.adapter.http.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InitPaymentRequestDto(UUID orderId, BigDecimal amount, String email, String name) {
}
