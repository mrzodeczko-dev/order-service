package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.RemoveItemFromOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;

/**
 * Handler for removing an item from a draft order.
 * Removes a product from the order and saves the updated order.
 */
public class RemoveItemFromOrderHandler {
    private final OrderRepository orderRepository;

    /**
     * Creates a new RemoveItemFromOrderHandler.
     * @param orderRepository the order repository
     */
    public RemoveItemFromOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Handles the RemoveItemFromOrderCommand.
     * @param command the command containing order ID and product ID to remove
     * @return the updated order
     * @throws IllegalArgumentException if order not found
     * @throws IllegalArgumentException if item with product ID not found in order
     */
    public Order handle(RemoveItemFromOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.removeItemByProductId(new ProductId(command.productId()));
        orderRepository.save(order);
        return order;
    }
}
