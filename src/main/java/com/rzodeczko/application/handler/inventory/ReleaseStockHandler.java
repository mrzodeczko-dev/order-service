package com.rzodeczko.application.handler.inventory;


import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

public class ReleaseStockHandler {
    private final InventoryRepository inventoryRepository;

    public ReleaseStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void handle(ReleaseStockCommand command) {
        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(
                        new StoreId(command.storeId()),
                        new ProductId(command.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));
        inventory.release(command.quantity());
        inventoryRepository.save(inventory);
    }
}
