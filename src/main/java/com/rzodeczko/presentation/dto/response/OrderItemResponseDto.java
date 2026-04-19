package com.rzodeczko.presentation.dto.response;

import java.util.UUID;

public record OrderItemResponseDto(UUID productId, int quantity, String unitPrice, String lineTotal) {
}
