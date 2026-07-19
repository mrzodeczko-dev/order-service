package com.rzodeczko.integration;

import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.entity.OrderEntity;
import com.rzodeczko.infrastructure.persistence.repository.JpaOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for OrderRepositoryAdapter with real MySQL via Testcontainers.
 * Validates JPA queries, entity mapping, and aggregate reconstruction.
 */
class OrderRepositoryAdapterIT extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    private static final Currency PLN = Currency.getInstance("PLN");

    @BeforeEach
    void setUp() {
        jpaOrderRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindOrderById() {
        OrderId orderId = OrderId.newId();
        StoreId storeId = StoreId.newId();
        Order order = new Order(orderId, storeId);

        orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(orderId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(orderId);
        assertThat(found.get().getStoreId()).isEqualTo(storeId);
        assertThat(found.get().getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    void shouldSaveOrderWithItems() {
        OrderId orderId = OrderId.newId();
        StoreId storeId = StoreId.newId();
        ProductId productId = ProductId.newId();
        Order order = new Order(orderId, storeId);
        order.addItem(new OrderItem(productId, 3, new Money(new BigDecimal("49.99"), PLN)));

        orderRepository.save(order);

        Order found = orderRepository.findById(orderId).orElseThrow();

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().getFirst().getProductId()).isEqualTo(productId);
        assertThat(found.getItems().getFirst().getQuantity()).isEqualTo(3);
        assertThat(found.getItems().getFirst().getUnitPrice().amount())
                .isEqualByComparingTo(new BigDecimal("49.99"));
        assertThat(found.getTotalAmount().amount())
                .isEqualByComparingTo(new BigDecimal("149.97"));
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        Optional<Order> found = orderRepository.findById(OrderId.newId());

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllOrders() {
        orderRepository.save(new Order(OrderId.newId(), StoreId.newId()));
        orderRepository.save(new Order(OrderId.newId(), StoreId.newId()));
        orderRepository.save(new Order(OrderId.newId(), StoreId.newId()));

        List<Order> all = orderRepository.findAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void shouldFindAwaitingPaymentOlderThanCutoff() {
        OrderId expiredId = OrderId.newId();
        StoreId storeId = StoreId.newId();

        // Expired AWAITING_PAYMENT order
        jpaOrderRepository.saveAndFlush(OrderEntity.builder()
                .id(expiredId.id())
                .storeId(storeId.id())
                .status(OrderStatus.AWAITING_PAYMENT)
                .totalAmount(BigDecimal.TEN)
                .currency("PLN")
                .buyerEmail("b@e.com")
                .buyerName("Name")
                .buyerTaxId("123")
                .paymentId(UUID.randomUUID())
                .awaitingPaymentSince(Instant.now().minus(2, ChronoUnit.HOURS))
                .build());

        // Recent AWAITING_PAYMENT order - should NOT be found
        jpaOrderRepository.saveAndFlush(OrderEntity.builder()
                .id(UUID.randomUUID())
                .storeId(storeId.id())
                .status(OrderStatus.AWAITING_PAYMENT)
                .totalAmount(BigDecimal.TEN)
                .currency("PLN")
                .buyerEmail("b@e.com")
                .buyerName("Name")
                .buyerTaxId("123")
                .paymentId(UUID.randomUUID())
                .awaitingPaymentSince(Instant.now().plus(1, ChronoUnit.HOURS))
                .build());

        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        List<Order> expired = orderRepository.findAwaitingPaymentOlderThan(cutoff);

        assertThat(expired).hasSize(1);
        assertThat(expired.getFirst().getId()).isEqualTo(expiredId);
    }

    @Test
    void shouldFindDraftWithPaymentOlderThanCutoff() {
        StoreId storeId = StoreId.newId();
        OrderId orphanedId = OrderId.newId();

        // Orphaned DRAFT with paymentId
        jpaOrderRepository.saveAndFlush(OrderEntity.builder()
                .id(orphanedId.id())
                .storeId(storeId.id())
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.TEN)
                .currency("PLN")
                .paymentId(UUID.randomUUID())
                .awaitingPaymentSince(Instant.now().minus(2, ChronoUnit.HOURS))
                .build());

        // Normal DRAFT without paymentId - should NOT be found
        jpaOrderRepository.saveAndFlush(OrderEntity.builder()
                .id(UUID.randomUUID())
                .storeId(storeId.id())
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .currency("PLN")
                .build());

        Instant cutoff = Instant.now().minus(1, ChronoUnit.HOURS);
        List<Order> orphaned = orderRepository.findDraftWithPaymentOlderThan(cutoff);

        assertThat(orphaned).hasSize(1);
        assertThat(orphaned.getFirst().getId()).isEqualTo(orphanedId);
    }

    @Test
    void shouldSumQuantityOfProductInDraftOrders() {
        StoreId storeId = StoreId.newId();
        ProductId productId = ProductId.newId();

        // Draft order with 3 of productId
        Order order1 = new Order(OrderId.newId(), storeId);
        order1.addItem(new OrderItem(productId, 3, new Money(BigDecimal.TEN, PLN)));
        orderRepository.save(order1);

        // Another draft order with 2 of same productId
        Order order2 = new Order(OrderId.newId(), storeId);
        order2.addItem(new OrderItem(productId, 2, new Money(BigDecimal.TEN, PLN)));
        orderRepository.save(order2);

        int sum = orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId);

        assertThat(sum).isEqualTo(5);
    }

    @Test
    void shouldReturnZeroWhenNoDraftOrdersForProduct() {
        int sum = orderRepository.sumQuantityOfProductInDraftOrders(
                ProductId.newId(), StoreId.newId());

        assertThat(sum).isEqualTo(0);
    }

    @Test
    void shouldPreserveOrderStatusTransitions() {
        OrderId orderId = OrderId.newId();
        StoreId storeId = StoreId.newId();
        ProductId productId = ProductId.newId();
        UUID paymentId = UUID.randomUUID();

        Order order = new Order(orderId, storeId);
        order.addItem(new OrderItem(productId, 1, new Money(BigDecimal.TEN, PLN)));
        order.assignBuyerDetails("b@e.com", "Name", "123");
        order.place();
        order.markAwaitingPayment(paymentId, "https://pay.com");
        order.markPaid(paymentId);
        orderRepository.save(order);

        Order found = orderRepository.findById(orderId).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(found.getPaymentId()).isEqualTo(paymentId);
        assertThat(found.getBuyerEmail()).isEqualTo("b@e.com");
        assertThat(found.getBuyerName()).isEqualTo("Name");
        assertThat(found.getBuyerTaxId()).isEqualTo("123");
        assertThat(found.getItems()).hasSize(1);
    }
}
