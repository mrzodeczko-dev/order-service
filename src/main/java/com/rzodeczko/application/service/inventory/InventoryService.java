package com.rzodeczko.application.service.inventory;

import java.util.UUID;

public interface InventoryService {
    void reserve(UUID storeId, UUID productId, int quantity);
    void release(UUID storeId, UUID productId, int quantity);
    void replenish(UUID storeId, UUID productId, int quantity);
}
