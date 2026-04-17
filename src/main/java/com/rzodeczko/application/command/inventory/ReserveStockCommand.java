package com.rzodeczko.application.command.inventory;

import java.util.UUID;

public record ReserveStockCommand(
        UUID storeId,
        UUID productId,
        int quantity
) {
}
