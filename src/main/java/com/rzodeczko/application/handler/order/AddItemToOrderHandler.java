package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.application.command.order.AddItemToOrderCommand;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.exception.ProductNotFoundException;
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
 * Handler for adding an item to a draft order.
 * Validates stock availability, merges items with same product and price,
 * and saves the updated order.
 */
public class AddItemToOrderHandler {

    /** The order repository. */
    private final OrderRepository orderRepository;

    /** The product repository. */
    private final ProductRepository productRepository;

    /** The stock availability handler. */
    private final CheckStockAvailabilityHandler checkStockAvailabilityHandler;

    /**
     * Creates a new AddItemToOrderHandler.
     * @param orderRepository the order repository
     * @param productRepository the product repository
     * @param checkStockAvailabilityHandler the stock availability handler
     */
    public AddItemToOrderHandler(OrderRepository orderRepository, ProductRepository productRepository, CheckStockAvailabilityHandler checkStockAvailabilityHandler) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.checkStockAvailabilityHandler = checkStockAvailabilityHandler;
    }

    /**
     * Handles the AddItemToOrderCommand.
     * Adds or merges a product item to the draft order after validating stock availability.
     * @param command the command containing order ID, product ID, quantity and optional unit price
     * @return the updated order
     * @throws IllegalArgumentException if order or product not found
     * @throws IllegalStateException if order is not in DRAFT status or insufficient stock
     */
    public Order handle(AddItemToOrderCommand command) {
        Order order = orderRepository
                .findById(new OrderId(command.orderId()))
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        if (!order.isDraft()) {
            throw new InvalidOrderStateException("Can modify order only in DRAFT state");
        }

        Product product = productRepository
                .findById(new ProductId(command.productId()))
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Money unitPrice = command.unitPrice() != null
                ? new Money(command.unitPrice(), Currency.getInstance("PLN"))
                : product.getUnitPrice();

        /**
         * Sum quantity of this product already in the order (regardless of price).
         * Warehouse sees the product, not the price - otherwise one could bypass limits by adding
         * the same product with different prices.
         */
        int alreadyInOrder = order
                .getItems()
                .stream()
                .filter(item -> item.getProductId().id().equals(command.productId()))
                .mapToInt(OrderItem::getQuantity)
                .sum();

        checkStockAvailabilityHandler.handle(new CheckStockAvailabilityCommand(
                order.getStoreId().id(),
                command.productId(),
                command.quantity() + alreadyInOrder
        ));

        /**
         * Merge items with the same product and price - do not create duplicates.
         */
        order
                .getItems()
                .stream()
                .filter(item -> item.isSameProductAndPrice(
                        new ProductId(command.productId()),
                        unitPrice))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.changeQuantity(
                                existing.getQuantity() + command.quantity()),
                        () -> order.addItem(new OrderItem(
                                new ProductId(command.productId()),
                                command.quantity(),
                                unitPrice
                        ))
                );

        orderRepository.save(order);
        return order;
    }
}
