package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReserveStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

/**
 * Handler for reserving stock.
 * Reserves inventory quantity for an order and saves the updated inventory.
 */
public class ReserveStockHandler {

    /** The inventory repository. */
    private final InventoryRepository inventoryRepository;

    /**
     * Creates a new ReserveStockHandler.
     * @param inventoryRepository the inventory repository
     */
    public ReserveStockHandler(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Handles the ReserveStockCommand.
     * @param command the command containing store ID, product ID and quantity to reserve
     * @throws IllegalArgumentException if inventory not found
     * @throws IllegalArgumentException if reserve quantity is invalid
     */
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
