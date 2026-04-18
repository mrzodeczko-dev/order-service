package com.rzodeczko.domain.repository;

import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order aggregate.
 * Provides data access operations for order persistence.
 */
public interface OrderRepository {

    /**
     * Saves an order to the repository.
     * @param order the order to save
     */
    void save(Order order);

    /**
     * Finds an order by its ID.
     * @param id the order ID
     * @return an Optional containing the order if found
     */
    Optional<Order> findById(OrderId id);

    /**
     * Retrieves all orders.
     * @return a list of all orders
     */
    List<Order> findAll();

    /**
     * Finds orders awaiting payment that are older than the specified cutoff.
     * Used by the expiration scheduler to handle expired awaiting payment orders.
     * @param cutoff the cutoff instant
     * @return a list of orders awaiting payment older than the cutoff
     */
    List<Order> findAwaitingPaymentOlderThan(Instant cutoff);

    /**
     * Finds draft orders with payment ID that are older than the specified cutoff.
     * Used by the reconciliation job to handle orphaned draft orders with failed payments.
     * @param cutoff the cutoff instant
     * @return a list of draft orders with payment ID older than the cutoff
     */
    List<Order> findDraftWithPaymentOlderThan(Instant cutoff);

    /**
     * Sums the quantity of a product across all draft orders in a store.
     * Used by CheckStockAvailabilityHandler to account for draft reservations.
     * @param productId the product ID
     * @param storeId the store ID
     * @return the total quantity of the product in draft orders
     */
    int sumQuantityOfProductInDraftOrders(ProductId productId, StoreId storeId);
}
