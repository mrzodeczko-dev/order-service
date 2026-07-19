package com.rzodeczko.infrastructure.scheduler;

import com.rzodeczko.application.service.order.OrderLifecycleService;
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

class ExpireAwaitingPaymentOrdersJobTest {

    private ExpireAwaitingPaymentOrdersJob job;
    private OrderRepository orderRepository;
    private OrderLifecycleService orderLifecycleService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        orderLifecycleService = mock(OrderLifecycleService.class);
        var properties = new SchedulerProperties(
                new SchedulerProperties.ExpireAwaitingPayment(5000, 30),
                new SchedulerProperties.InvoiceOutbox(5000),
                new SchedulerProperties.Reconciliation(60000, 60)
        );
        job = new ExpireAwaitingPaymentOrdersJob(orderRepository, orderLifecycleService, properties);
    }

    @Test
    void shouldCancelExpiredOrders() {
        // given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        Order order1 = Order.restore(new OrderId(orderId1), new StoreId(UUID.randomUUID()),
                OrderStatus.AWAITING_PAYMENT, "a@b.com", "A", "TAX1",
                UUID.randomUUID(), null, null, Instant.now().minusSeconds(3600));
        Order order2 = Order.restore(new OrderId(orderId2), new StoreId(UUID.randomUUID()),
                OrderStatus.AWAITING_PAYMENT, "c@d.com", "B", "TAX2",
                UUID.randomUUID(), null, null, Instant.now().minusSeconds(3600));

        when(orderRepository.findAwaitingPaymentOlderThan(any(Instant.class))).thenReturn(List.of(order1, order2));

        // when
        job.expireOrders();

        // then
        verify(orderLifecycleService).cancelOrder(orderId1);
        verify(orderLifecycleService).cancelOrder(orderId2);
    }

    @Test
    void shouldDoNothingWhenNoExpiredOrders() {
        // given
        when(orderRepository.findAwaitingPaymentOlderThan(any(Instant.class))).thenReturn(Collections.emptyList());

        // when
        job.expireOrders();

        // then
        verifyNoInteractions(orderLifecycleService);
    }

    @Test
    void shouldContinueProcessingWhenOneOrderFailsToCanel() {
        // given
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        Order order1 = Order.restore(new OrderId(orderId1), new StoreId(UUID.randomUUID()),
                OrderStatus.AWAITING_PAYMENT, "a@b.com", "A", "TAX1",
                UUID.randomUUID(), null, null, Instant.now().minusSeconds(3600));
        Order order2 = Order.restore(new OrderId(orderId2), new StoreId(UUID.randomUUID()),
                OrderStatus.AWAITING_PAYMENT, "c@d.com", "B", "TAX2",
                UUID.randomUUID(), null, null, Instant.now().minusSeconds(3600));

        when(orderRepository.findAwaitingPaymentOlderThan(any())).thenReturn(List.of(order1, order2));
        doThrow(new RuntimeException("cancel failed")).when(orderLifecycleService).cancelOrder(orderId1);

        // when
        job.expireOrders();

        // then
        verify(orderLifecycleService).cancelOrder(orderId1);
        verify(orderLifecycleService).cancelOrder(orderId2);
    }
}
