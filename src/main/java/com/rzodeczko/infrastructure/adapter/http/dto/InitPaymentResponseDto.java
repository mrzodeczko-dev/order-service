package com.rzodeczko.infrastructure.adapter.http.dto;

import java.util.UUID;

public record InitPaymentResponseDto(UUID paymentId, String redirectUrl) {
}
