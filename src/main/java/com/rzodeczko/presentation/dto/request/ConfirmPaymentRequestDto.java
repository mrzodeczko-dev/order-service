package com.rzodeczko.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmPaymentRequestDto(
        @NotNull UUID paymentId
) {
}
