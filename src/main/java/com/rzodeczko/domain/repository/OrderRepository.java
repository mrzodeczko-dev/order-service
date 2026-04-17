package com.rzodeczko.domain.repository;


import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAll();

    // Dla schedulera wygasania - zamowienia AWAITING_PAYMENT starsze niz cutoff
    List<Order> findAwaitingPaymentOlderThan(Instant cutoff);

    // Dla reconciliation job - DRAFT z paymentId != null starsze niz cutoff
    List<Order> findDraftWithPaymentOlderThan(Instant cutoff);

    // Dla CheckStockAvailabilityHandler - ile sztuk produktu jest juz w DRAFT
    int sumQuantityOfProductInDraftOrders(ProductId productId, StoreId storeId);
}
