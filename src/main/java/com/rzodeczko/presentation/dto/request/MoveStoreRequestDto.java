package com.rzodeczko.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveStoreRequestDto(
        @NotNull UUID oldStoreId,
        @NotNull UUID newStoreId
) {
}
