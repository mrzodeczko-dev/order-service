package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

/**
 * Handler for releasing reserved stock.
 * Releases inventory reservation when order is cancelled and saves the updated inventory.
 */
public class ReleaseStockHandler {

    /** The inventory repository. */
    private final InventoryRepository inventoryRepository;

    /**
     * Creates a new ReleaseStockHandler.
     * @param inventoryRepository the inventory repository
     */
    public ReleaseStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Handles the ReleaseStockCommand.
     * @param command the command containing store ID, product ID and quantity to release
     * @throws IllegalArgumentException if inventory not found
     * @throws IllegalArgumentException if release quantity is invalid
     */
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
