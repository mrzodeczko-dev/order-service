package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.domain.exception.InsufficientStockException;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

/**
 * Handler for checking stock availability.
 * Validates that sufficient stock is available considering draft order reservations.
 */
public class CheckStockAvailabilityHandler {

    /**
     * The inventory repository.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * The order repository.
     */
    private final OrderRepository orderRepository;

    /**
     * Creates a new CheckStockAvailabilityHandler.
     *
     * @param inventoryRepository the inventory repository
     * @param orderRepository     the order repository
     */
    public CheckStockAvailabilityHandler(InventoryRepository inventoryRepository, OrderRepository orderRepository) {
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    public void handle(CheckStockAvailabilityCommand command) {
        StoreId storeId = new StoreId(command.storeId());
        ProductId productId = new ProductId(command.productId());

        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(storeId, productId)
                .orElseThrow(() -> new InventoryNotFoundException(storeId.id(), productId.id()));

        int draftReserved = orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId);
        int realAvailable = inventory.available() - draftReserved;

        if (command.requestedQuantity() > realAvailable) {
            throw new InsufficientStockException(
                    productId.id(),
                    command.requestedQuantity(),
                    realAvailable
            );
        }
    }
}
