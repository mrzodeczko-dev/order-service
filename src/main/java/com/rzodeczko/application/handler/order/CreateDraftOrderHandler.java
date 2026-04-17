package com.rzodeczko.application.handler.order;


import com.rzodeczko.application.command.order.CreateDraftOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;

public class CreateDraftOrderHandler {
    private final OrderRepository orderRepository;

    public CreateDraftOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order handle(CreateDraftOrderCommand command) {
        Order order = new Order(OrderId.newId(), new StoreId(command.storeId()));
        orderRepository.save(order);
        return order;
    }
}
