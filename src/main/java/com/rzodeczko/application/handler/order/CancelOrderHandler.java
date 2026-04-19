package com.rzodeczko.application.handler.order;


import com.rzodeczko.application.command.order.CancelOrderCommand;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;

/**
 * Handler for cancelling an order.
 * Transitions an order to CANCELLED status and saves it.
 */
public class CancelOrderHandler {
    private final OrderRepository orderRepository;

    /**
     * Creates a new CancelOrderHandler.
     * @param orderRepository the order repository
     */
    public CancelOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Handles the CancelOrderCommand.
     * @param command the command containing order ID
     * @return the cancelled order
     * @throws IllegalArgumentException if order not found
     * @throws IllegalStateException if order is in invalid status for cancellation
     */
    public Order handle(CancelOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));
        order.cancel();
        orderRepository.save(order);
        return order;
    }
}
