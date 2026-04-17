package com.rzodeczko.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for StoreId value object.
 */
class StoreIdTest {

    @Test
    void shouldCreateStoreIdWithValidUUID() {
        // given
        UUID uuid = UUID.randomUUID();

        // when
        StoreId storeId = new StoreId(uuid);

        // then
        assertThat(storeId.id()).isEqualTo(uuid);
    }

    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new StoreId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Store id cannot be null");
    }

    @Test
    void shouldCreateNewRandomStoreId() {
        // when
        StoreId storeId = StoreId.newId();

        // then
        assertThat(storeId.id()).isNotNull();
    }

    @Test
    void shouldBeEqualForSameUUID() {
        // given
        UUID uuid = UUID.randomUUID();
        StoreId storeId1 = new StoreId(uuid);
        StoreId storeId2 = new StoreId(uuid);

        // when & then
        assertThat(storeId1).isEqualTo(storeId2);
    }
}

