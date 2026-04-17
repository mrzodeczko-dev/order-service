package com.rzodeczko.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ProductId value object.
 */
class ProductIdTest {

    @Test
    void shouldCreateProductIdWithValidUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        ProductId productId = new ProductId(uuid);

        // then
        assertThat(productId.id()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new ProductId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Product id cannot be null");
    }

    @Test
    void shouldCreateNewRandomProductId() {
        // when
        ProductId productId = ProductId.newId();

        // then
        assertThat(productId.id()).isNotNull();
    }

    @Test
    void shouldBeEqualForSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        ProductId productId1 = new ProductId(uuid);
        ProductId productId2 = new ProductId(uuid);

        // when & then
        assertThat(productId1).isEqualTo(productId2);
    }
}

