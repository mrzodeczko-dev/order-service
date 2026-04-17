package com.rzodeczko.application.port;

import java.util.UUID;

public interface NotificationPort {
    void sendOrderConfirmation(UUID orderId, UUID paymentId, UUID invoiceId);
}
