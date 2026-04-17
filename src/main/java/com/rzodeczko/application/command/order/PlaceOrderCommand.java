package com.rzodeczko.application.command.order;

import java.util.UUID;

public record PlaceOrderCommand(
        UUID orderId,
        String buyerEmail,
        String buyerName,
        String buyerTaxId
) {
}
