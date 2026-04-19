package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.application.command.order.ReplaceProductInOrderCommand;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.repository.ProductRepository;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;

import java.util.Currency;

/**
 * Handler for replacing a product in a draft order.
 * Removes the old product and adds the new product after validating stock availability.
 */
public class ReplaceProductInOrderHandler {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CheckStockAvailabilityHandler checkStockAvailabilityHandler;

    /**
     * Creates a new ReplaceProductInOrderHandler.
     * @param orderRepository the order repository
     * @param productRepository the product repository
     * @param checkStockAvailabilityHandler the stock availability handler
     */
    public ReplaceProductInOrderHandler(OrderRepository orderRepository, ProductRepository productRepository, CheckStockAvailabilityHandler checkStockAvailabilityHandler) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.checkStockAvailabilityHandler = checkStockAvailabilityHandler;
    }

    /**
     * Handles the ReplaceProductInOrderCommand.
     * @param command the command containing order ID, old product ID and new product details
     * @return the updated order
     * @throws IllegalArgumentException if order or product not found
     * @throws IllegalStateException if order is not in DRAFT status or insufficient stock
     */
    public Order handle(ReplaceProductInOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        if (!order.isDraft()) {
            throw new InvalidOrderStateException("Can replace items only in DRAFT state");
        }

        checkStockAvailabilityHandler.handle(new CheckStockAvailabilityCommand(
                command.storeId(),
                command.newProductId(),
                command.newQuantity()
        ));

        order.removeItemByProductId(new ProductId(command.oldProductId()));

        Product product = productRepository
                .findById(new ProductId(command.newProductId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        Money unitPrice = command.newUnitPrice() != null
                ? new Money(command.newUnitPrice(), Currency.getInstance("PLN"))
                : product.getUnitPrice();

        order.addItem(new OrderItem(
                new ProductId(command.newProductId()),
                command.newQuantity(),
                unitPrice
        ));

        orderRepository.save(order);
        return order;
    }
}
