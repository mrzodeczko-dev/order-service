package com.rzodeczko.infrastructure.adapter;

import com.rzodeczko.application.port.NotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class MockNotificationAdapter implements NotificationPort {
    @Override
    public void sendOrderConfirmation(UUID orderId, UUID paymentId, UUID invoiceId) {
        log.info("Sending confirmation for orderId={}, paymentId={}, invoiceId={}", orderId, paymentId, invoiceId);
    }
}
