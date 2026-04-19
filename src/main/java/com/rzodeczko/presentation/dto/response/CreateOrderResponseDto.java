package com.rzodeczko.presentation.dto.response;

import java.util.UUID;

public record CreateOrderResponseDto(UUID orderId, String status) {
}
