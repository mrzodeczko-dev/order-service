package com.rzodeczko.infrastructure.outbox;

import com.rzodeczko.application.port.InvoicePort;
import com.rzodeczko.application.port.NotificationPort;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.repository.ProductRepository;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InvoiceOutboxSenderTest {

    private InvoiceOutboxSender sender;
    private InvoicePort invoicePort;
    private NotificationPort notificationPort;
    private OrderRepository orderRepository;
    private InvoiceOutboxTaskRepository outboxRepository;
    private ProductRepository productRepository;

    private UUID orderId;
    private UUID productIdValue;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        invoicePort = mock(InvoicePort.class);
        notificationPort = mock(NotificationPort.class);
        orderRepository = mock(OrderRepository.class);
        outboxRepository = mock(InvoiceOutboxTaskRepository.class);
        productRepository = mock(ProductRepository.class);
        sender = new InvoiceOutboxSender(invoicePort, notificationPort, orderRepository, outboxRepository, productRepository);

        orderId = UUID.randomUUID();
        productIdValue = UUID.randomUUID();
        paymentId = UUID.randomUUID();
    }

    private Order buildPaidOrder() {
        Order order = Order.restore(
                new OrderId(orderId), new StoreId(UUID.randomUUID()), OrderStatus.PAID,
                "buyer@test.com", "Buyer Name", "TAX123",
                paymentId, null, null, null
        );
        order.addItemForRestore(new OrderItem(
                new ProductId(productIdValue), 2,
                new Money(BigDecimal.TEN, Currency.getInstance("PLN"))
        ));
        return order;
    }

    @Test
    void shouldGenerateInvoiceAndMarkTaskAsSent() {
        // given
        Order order = buildPaidOrder();
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        Product product = new Product(new ProductId(productIdValue), "SKU-1", "Widget",
                new Money(BigDecimal.TEN, Currency.getInstance("PLN")), BigDecimal.valueOf(0.23), true);
        when(productRepository.findById(new ProductId(productIdValue))).thenReturn(Optional.of(product));

        UUID invoiceId = UUID.randomUUID();
        when(invoicePort.generateInvoice(eq(orderId), eq("TAX123"), eq("Buyer Name"), anyList())).thenReturn(invoiceId);

        // when
        sender.send(task);

        // then
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.SENT);
        verify(orderRepository).save(order);
        verify(outboxRepository).save(task);
        verify(notificationPort).sendOrderConfirmation(orderId, paymentId, invoiceId);
    }

    @Test
    void shouldSkipWhenInvoiceAlreadyAssigned() {
        // given
        UUID existingInvoiceId = UUID.randomUUID();
        Order order = Order.restore(
                new OrderId(orderId), new StoreId(UUID.randomUUID()), OrderStatus.PAID,
                "buyer@test.com", "Buyer Name", "TAX123",
                paymentId, existingInvoiceId, null, null
        );
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        // when
        sender.send(task);

        // then
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.SENT);
        verify(outboxRepository).save(task);
        verifyNoInteractions(invoicePort);
    }

    @Test
    void shouldMarkTaskAsFailedOnInvoicePortError() {
        // given
        Order order = buildPaidOrder();
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));
        when(productRepository.findById(any())).thenReturn(Optional.empty());
        when(invoicePort.generateInvoice(any(), any(), any(), anyList())).thenThrow(new RuntimeException("HTTP error"));

        // when
        sender.send(task);

        // then
        assertThat(task.getRetryCount()).isEqualTo(1);
        verify(outboxRepository).save(task);
    }

    @Test
    void shouldNotFailWhenNotificationFails() {
        // given
        Order order = buildPaidOrder();
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        UUID invoiceId = UUID.randomUUID();
        when(invoicePort.generateInvoice(any(), any(), any(), anyList())).thenReturn(invoiceId);
        doThrow(new RuntimeException("notification failed")).when(notificationPort).sendOrderConfirmation(any(), any(), any());

        // when
        sender.send(task);

        // then
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.SENT);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        // given
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);
        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> sender.send(task))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldUseUnknownProductNameWhenProductNotFound() {
        // given
        Order order = buildPaidOrder();
        InvoiceOutboxTask task = new InvoiceOutboxTask(UUID.randomUUID(), orderId, InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));
        when(productRepository.findById(any())).thenReturn(Optional.empty());

        UUID invoiceId = UUID.randomUUID();
        when(invoicePort.generateInvoice(eq(orderId), eq("TAX123"), eq("Buyer Name"), anyList())).thenReturn(invoiceId);

        // when
        sender.send(task);

        // then
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.SENT);
        verify(invoicePort).generateInvoice(eq(orderId), eq("TAX123"), eq("Buyer Name"), anyList());
    }
}
