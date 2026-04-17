package com.rzodeczko.domain.model.inventory;

import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Inventory.
 */
class InventoryTest {

    private Inventory inventory;
    private StoreId storeId;
    private ProductId productId;

    @BeforeEach
    void setUp() {
        storeId = StoreId.newId();
        productId = ProductId.newId();
        inventory = new Inventory(storeId, productId, 100, 10);
    }

    @Test
    void shouldCreateInventory() {
        // when & then
        assertThat(inventory.getId()).isNotNull();
        assertThat(inventory.getStoreId()).isEqualTo(storeId);
        assertThat(inventory.getProductId()).isEqualTo(productId);
        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        assertThat(inventory.getQuantityReserved()).isEqualTo(10);
        assertThat(inventory.getVersion()).isNull();
    }

    @Test
    void shouldCalculateAvailableQuantity() {
        // when
        int available = inventory.available();

        // then
        assertThat(available).isEqualTo(90); // 100 - 10
    }

    @Test
    void shouldReturnZeroAvailableWhenNothingInStock() {
        // given
        Inventory inv = new Inventory(storeId, productId, 10, 10);

        // when
        int available = inv.available();

        // then
        assertThat(available).isZero();
    }

    @Test
    void shouldReserveQuantity() {
        // when
        inventory.reserve(20);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(30); // 10 + 20
        assertThat(inventory.available()).isEqualTo(70); // 100 - 30
    }

    @Test
    void shouldThrowExceptionWhenReservingZeroQuantity() {
        // when & then
        assertThatThrownBy(() -> inventory.reserve(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to reserve must be positive");
    }

    @Test
    void shouldThrowExceptionWhenReservingNegativeQuantity() {
        // when & then
        assertThatThrownBy(() -> inventory.reserve(-5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to reserve must be positive");
    }

    @Test
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        // when & then
        assertThatThrownBy(() -> inventory.reserve(100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough stock to reserve");
    }

    @Test
    void shouldReleaseQuantity() {
        // when
        inventory.release(5);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(5); // 10 - 5
        assertThat(inventory.available()).isEqualTo(95); // 100 - 5
    }

    @Test
    void shouldThrowExceptionWhenReleasingZeroQuantity() {
        // when & then
        assertThatThrownBy(() -> inventory.release(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to release must be positive");
    }

    @Test
    void shouldThrowExceptionWhenReleasingMoreThanReserved() {
        // when & then
        assertThatThrownBy(() -> inventory.release(50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot release more than reserved");
    }

    @Test
    void shouldReplenishQuantity() {
        // when
        inventory.replenish(50);

        // then
        assertThat(inventory.getQuantityOnHand()).isEqualTo(150); // 100 + 50
    }

    @Test
    void shouldThrowExceptionWhenReplenishingZeroQuantity() {
        // when & then
        assertThatThrownBy(() -> inventory.replenish(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
    }

    @Test
    void shouldUpdateQuantityWhenFulfilled() {
        // when
        inventory.updateQuantityWhenFulfilled(5);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(5); // 10 - 5 (released)
        assertThat(inventory.getQuantityOnHand()).isEqualTo(95); // 100 - 5 (shipped)
        assertThat(inventory.available()).isEqualTo(90); // 95 - 5
    }

    @Test
    void shouldThrowExceptionWhenFulfillingZeroQuantity() {
        // when & then
        assertThatThrownBy(() -> inventory.updateQuantityWhenFulfilled(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");
    }
}

