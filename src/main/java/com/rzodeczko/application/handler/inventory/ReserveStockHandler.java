package com.rzodeczko.application.handler.inventory;


import com.rzodeczko.application.command.inventory.ReserveStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

public class ReserveStockHandler {
    private final InventoryRepository inventoryRepository;

    public ReserveStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void handle(ReserveStockCommand command) {
        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(
                        new StoreId(command.storeId()),
                        new ProductId(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.reserve(command.quantity());
        inventoryRepository.save(inventory);
    }
}
