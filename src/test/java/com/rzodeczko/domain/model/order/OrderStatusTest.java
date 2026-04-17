package com.rzodeczko.domain.model.order;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for OrderStatus enum.
 */
class OrderStatusTest {

    @Test
    void shouldHaveAllRequiredStatuses() {
        // when & then
        assertThat(OrderStatus.values())
                .containsExactly(
                        OrderStatus.DRAFT,
                        OrderStatus.PLACED,
                        OrderStatus.AWAITING_PAYMENT,
                        OrderStatus.PAID,
                        OrderStatus.FULFILLED,
                        OrderStatus.CANCELLED
                );
    }

    @Test
    void shouldHaveDraftStatus() {
        // when & then
        assertThat(OrderStatus.DRAFT).isNotNull();
    }

    @Test
    void shouldHavePlacedStatus() {
        // when & then
        assertThat(OrderStatus.PLACED).isNotNull();
    }
}

