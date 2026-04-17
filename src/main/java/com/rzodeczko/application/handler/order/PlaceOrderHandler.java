package com.rzodeczko.application.handler.order;


import com.rzodeczko.application.command.order.PlaceOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;

public class PlaceOrderHandler {
    private final OrderRepository orderRepository;

    public PlaceOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order handle(PlaceOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new IllegalArgumentException("Order Not Found"));

        order.assignBuyerDetails(
                command.buyerEmail(),
                command.buyerName(),
                command.buyerTaxId()
        );
        order.place();
        // Nie zapisujemy tutaj - PlaceOrderHandler tylko zmienia stan agregatu.
        // Zapis nastapi w savePlacedOrderAtomically razem z rezerwacja stocku
        return order;
    }
}
