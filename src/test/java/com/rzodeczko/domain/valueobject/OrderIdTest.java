package com.rzodeczko.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OrderId value object.
 */
class OrderIdTest {

    @Test
    void shouldCreateOrderIdWithValidUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        OrderId orderId = new OrderId(uuid);

        // then
        assertThat(orderId.id()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new OrderId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order id cannot be null");
    }

    @Test
    void shouldCreateNewRandomOrderId() {
        // when
        OrderId orderId = OrderId.newId();

        // then
        assertThat(orderId.id()).isNotNull();
    }

    @Test
    void shouldBeEqualForSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        OrderId orderId1 = new OrderId(uuid);
        OrderId orderId2 = new OrderId(uuid);

        // when & then
        assertThat(orderId1).isEqualTo(orderId2);
    }

    @Test
    void shouldNotBeEqualForDifferentUUIDs() {
        // given
        OrderId orderId1 = OrderId.newId();
        OrderId orderId2 = OrderId.newId();

        // when & then
        assertThat(orderId1).isNotEqualTo(orderId2);
    }
}

