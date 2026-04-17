package com.rzodeczko.application.command.inventory;

import java.util.UUID;

public record ReplenishStockCommand(
        UUID storeId,
        UUID productId,
        int quantity
) {
}
