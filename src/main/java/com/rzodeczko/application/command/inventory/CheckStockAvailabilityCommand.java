package com.rzodeczko.application.command.inventory;

import java.util.UUID;

public record CheckStockAvailabilityCommand(
        UUID storeId,
        UUID productId,
        int requestedQuantity
) {
}
