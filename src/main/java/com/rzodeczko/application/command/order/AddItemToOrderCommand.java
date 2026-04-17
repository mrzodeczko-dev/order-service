package com.rzodeczko.application.command.order;

import java.math.BigDecimal;
import java.util.UUID;

public record AddItemToOrderCommand(
        UUID orderId,
        UUID productId,
        int quantity,
        BigDecimal unitPrice
) {
}
