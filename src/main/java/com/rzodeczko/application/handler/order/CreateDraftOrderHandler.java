package com.rzodeczko.application.handler.order;


import com.rzodeczko.application.command.order.CreateDraftOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;

/**
 * Handler for creating a new draft order.
 * Creates a new order in DRAFT status and saves it to the repository.
 */
public class CreateDraftOrderHandler {
    private final OrderRepository orderRepository;

    /**
     * Creates a new CreateDraftOrderHandler.
     * @param orderRepository the order repository
     */
    public CreateDraftOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Handles the CreateDraftOrderCommand.
     * @param command the command containing store ID
     * @return the created draft order
     */
    public Order handle(CreateDraftOrderCommand command) {
        Order order = new Order(OrderId.newId(), new StoreId(command.storeId()));
        orderRepository.save(order);
        return order;
    }
}
