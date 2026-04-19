package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReplenishStockCommand;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

/**
 * Handler for replenishing stock.
 * Adds quantity to inventory and saves the updated inventory.
 */
public class ReplenishStockHandler {

    /** The inventory repository. */
    private final InventoryRepository inventoryRepository;

    /**
     * Creates a new ReplenishStockHandler.
     * @param inventoryRepository the inventory repository
     */
    public ReplenishStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Handles the ReplenishStockCommand.
     * @param command the command containing store ID, product ID and quantity to add
     * @throws IllegalArgumentException if inventory not found
     * @throws IllegalArgumentException if replenish quantity is invalid
     */
    public void handle(ReplenishStockCommand command) {
        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(
                        new StoreId(command.storeId()),
                        new ProductId(command.productId()))
                .orElseThrow(() -> new InventoryNotFoundException(command.storeId(), command.productId()));
        inventory.replenish(command.quantity());
        inventoryRepository.save(inventory);
    }
}
