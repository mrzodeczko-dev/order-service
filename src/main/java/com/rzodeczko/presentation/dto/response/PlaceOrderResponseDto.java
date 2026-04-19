package com.rzodeczko.presentation.dto.response;

import java.util.UUID;

public record PlaceOrderResponseDto(
        OrderSummaryResponseDto order,
        UUID paymentId,
        String paymentRedirectUrl
) {
}
