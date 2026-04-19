package com.rzodeczko.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record ReplaceItemRequestDto(
        @NotNull UUID oldProductId,
        @NotNull UUID newProductId,
        @Min(1) int newQuantity,
        @NotNull @DecimalMin("0.01") BigDecimal newUnitPrice,
        @NotNull UUID storeId
) {
}
