package com.rzodeczko.application.port;

import java.util.UUID;

/**
 * Port for sending notifications to customers.
 */
public interface NotificationPort {
    /**
     * Sends an order confirmation notification.
     * @param orderId the order ID
     * @param paymentId the payment ID
     * @param invoiceId the invoice ID
     */
    void sendOrderConfirmation(UUID orderId, UUID paymentId, UUID invoiceId);
}
