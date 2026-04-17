package com.rzodeczko.application.handler.order;


import com.rzodeczko.application.command.order.PlaceOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;

/**
 * Handler for placing an order.
 * Assigns buyer details to a draft order and transitions it to PLACED status.
 * The order is not saved here - it's saved atomically with stock reservation.
 */
public class PlaceOrderHandler {
    private final OrderRepository orderRepository;

    /**
     * Creates a new PlaceOrderHandler.
     * @param orderRepository the order repository
     */
    public PlaceOrderHandler(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Handles the PlaceOrderCommand.
     * @param command the command containing order ID and buyer details
     * @return the placed order
     * @throws IllegalArgumentException if order not found
     * @throws IllegalStateException if order is not in DRAFT state or has no items or missing buyer details
     */
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
