package com.rzodeczko.application.command.inventory;

import java.util.UUID;

public record ReleaseStockCommand(
        UUID storeId,
        UUID productId,
        int quantity
) {
}
