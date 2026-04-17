package com.rzodeczko.application.command.order;

import java.math.BigDecimal;
import java.util.UUID;

public record ReplaceProductInOrderCommand(
        UUID orderId,
        UUID oldProductId,
        UUID newProductId,
        int newQuantity,
        BigDecimal newUnitPrice,
        UUID storeId
) {
}
