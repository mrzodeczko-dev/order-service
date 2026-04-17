package com.rzodeczko.domain.model.order;

import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OrderItem.
 */
class OrderItemTest {

    @Test
    void shouldCreateOrderItemWithValidData() {
        // given
        ProductId productId = ProductId.newId();
        int quantity = 5;
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));

        // when
        OrderItem orderItem = new OrderItem(productId, quantity, unitPrice);

        // then
        assertThat(orderItem.getProductId()).isEqualTo(productId);
        assertThat(orderItem.getQuantity()).isEqualTo(quantity);
        assertThat(orderItem.getUnitPrice()).isEqualTo(unitPrice);
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));

        // when & then
        assertThatThrownBy(() -> new OrderItem(productId, 0, unitPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive.");
    }

    @Test
    void shouldThrowExceptionWhenQuantityIsNegative() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));

        // when & then
        assertThatThrownBy(() -> new OrderItem(productId, -5, unitPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive.");
    }

    @Test
    void shouldCalculateLineTotal() {
        // given
        ProductId productId = ProductId.newId();
        int quantity = 5;
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId, quantity, unitPrice);

        // when
        Money lineTotal = orderItem.lineTotal();

        // then
        assertThat(lineTotal.amount()).isEqualByComparingTo(new BigDecimal("52.50"));
        assertThat(lineTotal.currency()).isEqualTo(Currency.getInstance("PLN"));
    }

    @Test
    void shouldChangeQuantity() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId, 5, unitPrice);

        // when
        orderItem.changeQuantity(10);

        // then
        assertThat(orderItem.getQuantity()).isEqualTo(10);
    }

    @Test
    void shouldThrowExceptionWhenChangingQuantityToZero() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId, 5, unitPrice);

        // when & then
        assertThatThrownBy(() -> orderItem.changeQuantity(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive.");
    }

    @Test
    void shouldCheckIfSameProductAndPrice() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId, 5, unitPrice);

        // when
        boolean isSame = orderItem.isSameProductAndPrice(productId, unitPrice);

        // then
        assertThat(isSame).isTrue();
    }

    @Test
    void shouldReturnFalseWhenProductIdDiffers() {
        // given
        ProductId productId1 = ProductId.newId();
        ProductId productId2 = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId1, 5, unitPrice);

        // when
        boolean isSame = orderItem.isSameProductAndPrice(productId2, unitPrice);

        // then
        assertThat(isSame).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPriceDiffers() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice1 = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Money unitPrice2 = new Money(new BigDecimal("20.50"), Currency.getInstance("PLN"));
        OrderItem orderItem = new OrderItem(productId, 5, unitPrice1);

        // when
        boolean isSame = orderItem.isSameProductAndPrice(productId, unitPrice2);

        // then
        assertThat(isSame).isFalse();
    }
}

