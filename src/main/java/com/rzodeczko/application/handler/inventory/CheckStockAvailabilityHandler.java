package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

public class CheckStockAvailabilityHandler {
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    public CheckStockAvailabilityHandler(InventoryRepository inventoryRepository, OrderRepository orderRepository) {
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    public void handle(CheckStockAvailabilityCommand command) {
        StoreId storeId = new StoreId(command.storeId());
        ProductId productId = new ProductId(command.productId());

        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(storeId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        int draftReserved = orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId);
        int realAvailable = inventory.available() - draftReserved;

        if (command.requestedQuantity() > realAvailable) {
            throw new IllegalStateException(
                    "Not enough stock for product %s. Requested: %d, available: %d"
                            .formatted(
                                    productId.id(),
                                    command.requestedQuantity(),
                                    realAvailable
                            )
            );
        }
    }
}
