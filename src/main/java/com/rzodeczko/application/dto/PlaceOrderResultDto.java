package com.rzodeczko.application.dto;

import java.util.UUID;

public record PlaceOrderResultDto(
        OrderSummaryDto order,
        UUID paymentId,
        String paymentRedirectUrl
) {
}
