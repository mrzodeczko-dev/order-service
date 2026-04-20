package com.rzodeczko.domain.model.order;

import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Order aggregate.
 */
class OrderTest {

    private OrderId orderId;
    private StoreId storeId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = OrderId.newId();
        storeId = StoreId.newId();
        order = new Order(orderId, storeId);
    }

    @Test
    void shouldCreateOrderInDraftStatus() {
        // when & then
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getStoreId()).isEqualTo(storeId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.isDraft()).isTrue();
    }

    @Test
    void shouldAddItemToOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);

        // when
        order.addItem(item);

        // then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount().amount()).isEqualByComparingTo(new BigDecimal("21.00"));
    }

    @Test
    void shouldThrowExceptionWhenAddingItemToNonDraftOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();

        // when & then
        assertThatThrownBy(() -> order.addItem(item))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage("Can modify order only in DRAFT state");
    }

    @Test
    void shouldRemoveItemFromOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        // when
        order.removeItemByProductId(productId);

        // then
        assertThat(order.getItems()).isEmpty();
        assertThat(order.getTotalAmount()).isEqualTo(Money.ZERO_PLN);
    }

    @Test
    void shouldAssignBuyerDetails() {
        // when
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");

        // then
        assertThat(order.getBuyerEmail()).isEqualTo("buyer@example.com");
        assertThat(order.getBuyerName()).isEqualTo("John Doe");
        assertThat(order.getBuyerTaxId()).isEqualTo("123456789");
    }

    @Test
    void shouldThrowExceptionWhenAssigningNullEmail() {
        // when & then
        assertThatThrownBy(() -> order.assignBuyerDetails(null, "John Doe", "123456789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Buyer email is required");
    }

    @Test
    void shouldPlaceOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");

        // when
        order.place();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(order.isDraft()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenPlacingOrderWithoutItems() {
        // given
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");

        // when & then
        assertThatThrownBy(order::place)
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage("Order cannot be placed without items");
    }

    @Test
    void shouldMarkAwaitingPayment() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();

        // when
        order.markAwaitingPayment(paymentId, "https://payment.com/redirect");

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT);
        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        assertThat(order.getPaymentRedirectUrl()).isEqualTo("https://payment.com/redirect");
        assertThat(order.getAwaitingPaymentSince()).isNotNull();
    }

    @Test
    void shouldMarkPaid() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();
        order.markAwaitingPayment(paymentId, "https://payment.com/redirect");

        // when
        order.markPaid(paymentId);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void shouldFulfillOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        order.markAwaitingPayment(UUID.randomUUID(), "https://payment.com/redirect");
        order.markPaid(order.getPaymentId());

        // when
        order.fulfill();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.FULFILLED);
    }

    @Test
    void shouldCancelPlacedOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();

        // when
        order.cancel();

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldRelocateOrder() {
        // given
        StoreId newStoreId = StoreId.newId();

        // when
        order.relocate(newStoreId);

        // then
        assertThat(order.getStoreId()).isEqualTo(newStoreId);
    }

    @Test
    void shouldCheckIfReadyForFulfillment() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        order.markAwaitingPayment(UUID.randomUUID(), "https://payment.com/redirect");
        order.markPaid(order.getPaymentId());

        // when & then
        assertThat(order.isReadyForFulfillment()).isTrue();
    }
}

