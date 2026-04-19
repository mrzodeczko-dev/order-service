package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.FulfillOrderCommand;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.util.List;

/**
 * Handler for fulfilling a paid order.
 * Updates inventory quantities when order items are fulfilled and transitions order to FULFILLED status.
 */
public class FulfillOrderHandler {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Creates a new FulfillOrderHandler.
     *
     * @param orderRepository     the order repository
     * @param inventoryRepository the inventory repository
     */
    public FulfillOrderHandler(
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository
    ) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Handles the FulfillOrderCommand.
     * Updates inventory quantities for all items and marks order as FULFILLED.
     *
     * @param command the command containing order ID
     * @return the fulfilled order
     * @throws IllegalArgumentException if order or inventory not found
     * @throws IllegalStateException    if order is not in PAID status
     */
    public Order handle(FulfillOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        StoreId storeId = order.getStoreId();

        List<Inventory> updatedInventories = order
                .getItems()
                .stream()
                .map(item -> {
                    Inventory inventory = inventoryRepository
                            .findByStoreAndProduct(storeId, item.getProductId())
                            .orElseThrow(() -> new InventoryNotFoundException(storeId.id(), item.getProductId().id()));
                    inventory.updateQuantityWhenFulfilled(item.getQuantity());
                    return inventory;
                })
                .toList();

        inventoryRepository.saveAll(updatedInventories);
        order.fulfill();
        orderRepository.save(order);
        return order;
    }
}
