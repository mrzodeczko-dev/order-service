package com.rzodeczko.application.port;

import com.rzodeczko.application.port.data.PaymentInitData;
import com.rzodeczko.domain.model.order.Order;

import java.util.UUID;

/**
 * Port for atomic operations requiring @Transactional annotation.
 * Ensures data consistency for operations that must be executed as a single unit.
 */
public interface OrderAtomicPort {
    /**
     * Saves a placed order atomically with stock reservation and payment initialization.
     * All operations are performed in a single transaction:
     * - Reserve stock for order items
     * - Transition order from PLACED to AWAITING_PAYMENT
     * - Store payment ID and redirect URL
     * Prevents data inconsistency if HTTP call to payment service fails.
     *
     * @param order the order to save
     * @param paymentInitData the payment data (payment ID and redirect URL)
     */
    void savePlacedOrderAtomically(Order order, PaymentInitData paymentInitData);

    /**
     * Confirms payment atomically and creates invoice outbox task.
     * All operations are performed in a single transaction:
     * - Mark order as PAID
     * - Insert invoice outbox task for async invoice generation
     * Prevents crash between payment confirmation and invoice task creation.
     *
     * @param orderId the order ID
     * @param paymentId the confirmed payment ID
     */
    void confirmPaymentAtomically(UUID orderId, UUID paymentId);
}
