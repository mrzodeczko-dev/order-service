package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReplenishStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

public class ReplenishStockHandler {
    private final InventoryRepository inventoryRepository;

    public ReplenishStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void handle(ReplenishStockCommand command) {
        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(
                        new StoreId(command.storeId()),
                        new ProductId(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.replenish(command.quantity());
        inventoryRepository.save(inventory);
    }
}
