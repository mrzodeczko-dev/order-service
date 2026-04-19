package com.rzodeczko.domain.exception;

import java.util.UUID;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(UUID storeId, UUID productId) {
        super("Inventory not found for product %s in store %s".formatted(productId, storeId));
    }
}
