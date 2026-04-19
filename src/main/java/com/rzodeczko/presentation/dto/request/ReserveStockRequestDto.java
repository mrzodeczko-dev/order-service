package com.rzodeczko.presentation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReserveStockRequestDto(
        @NotNull UUID storeId,
        @NotNull UUID productId,
        @Min(1) int quantity
) {
}
