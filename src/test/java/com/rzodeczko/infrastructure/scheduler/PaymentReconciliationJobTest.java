package com.rzodeczko.infrastructure.scheduler;

import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.configuration.properties.SchedulerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentReconciliationJobTest {

    private PaymentReconciliationJob job;
    private OrderRepository orderRepository;
    private PaymentPort paymentPort;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        paymentPort = mock(PaymentPort.class);
        var properties = new SchedulerProperties(
                new SchedulerProperties.ExpireAwaitingPayment(5000, 30),
                new SchedulerProperties.InvoiceOutbox(5000),
                new SchedulerProperties.Reconciliation(60000, 60)
        );
        job = new PaymentReconciliationJob(orderRepository, paymentPort, properties);
    }

    @Test
    void shouldRefundOrphanedPaymentsAndClearData() {
        // given
        UUID paymentId = UUID.randomUUID();
        Order order = Order.restore(
                new OrderId(UUID.randomUUID()), new StoreId(UUID.randomUUID()), OrderStatus.DRAFT,
                "a@b.com", "Name", "TAX",
                paymentId, null, "http://redirect", Instant.now().minusSeconds(7200)
        );

        when(orderRepository.findDraftWithPaymentOlderThan(any(Instant.class))).thenReturn(List.of(order));

        // when
        job.reconcile();

        // then
        verify(paymentPort).refundPayment(paymentId);
        verify(orderRepository).save(order);
    }

    @Test
    void shouldDoNothingWhenNoOrphanedPayments() {
        // given
        when(orderRepository.findDraftWithPaymentOlderThan(any())).thenReturn(Collections.emptyList());

        // when
        job.reconcile();

        // then
        verifyNoInteractions(paymentPort);
    }

    @Test
    void shouldContinueProcessingOnRefundFailure() {
        // given
        UUID paymentId1 = UUID.randomUUID();
        UUID paymentId2 = UUID.randomUUID();
        Order order1 = Order.restore(
                new OrderId(UUID.randomUUID()), new StoreId(UUID.randomUUID()), OrderStatus.DRAFT,
                "a@b.com", "A", "TAX1",
                paymentId1, null, null, Instant.now().minusSeconds(7200)
        );
        Order order2 = Order.restore(
                new OrderId(UUID.randomUUID()), new StoreId(UUID.randomUUID()), OrderStatus.DRAFT,
                "c@d.com", "B", "TAX2",
                paymentId2, null, null, Instant.now().minusSeconds(7200)
        );

        when(orderRepository.findDraftWithPaymentOlderThan(any())).thenReturn(List.of(order1, order2));
        doThrow(new RuntimeException("refund failed")).when(paymentPort).refundPayment(paymentId1);

        // when
        job.reconcile();

        // then
        verify(paymentPort).refundPayment(paymentId1);
        verify(paymentPort).refundPayment(paymentId2);
    }
}
