package com.rzodeczko.domain.model.store;

import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Store.
 */
class StoreTest {

    @Test
    void shouldCreateStore() {
        // given
        StoreId storeId = StoreId.newId();

        // when
        Store store = new Store(storeId, "STORE-001", "Main Store", true);

        // then
        assertThat(store.getId()).isEqualTo(storeId);
        assertThat(store.getCode()).isEqualTo("STORE-001");
        assertThat(store.getName()).isEqualTo("Main Store");
        assertThat(store.isActive()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenStoreIdIsNull() {
        // when & then
        assertThatThrownBy(() -> new Store(null, "STORE-001", "Main Store", true))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Store id cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenCodeIsNull() {
        // given
        StoreId storeId = StoreId.newId();

        // when & then
        assertThatThrownBy(() -> new Store(storeId, null, "Main Store", true))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Store code cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        // given
        StoreId storeId = StoreId.newId();

        // when & then
        assertThatThrownBy(() -> new Store(storeId, "STORE-001", null, true))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Store name cannot be null");
    }

    @Test
    void shouldDeactivateStore() {
        // given
        StoreId storeId = StoreId.newId();
        Store store = new Store(storeId, "STORE-001", "Main Store", true);

        // when
        store.deactivate();

        // then
        assertThat(store.isActive()).isFalse();
    }

    @Test
    void shouldActivateStore() {
        // given
        StoreId storeId = StoreId.newId();
        Store store = new Store(storeId, "STORE-001", "Main Store", false);

        // when
        store.activate();

        // then
        assertThat(store.isActive()).isTrue();
    }
}

