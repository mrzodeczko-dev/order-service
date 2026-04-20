package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReleaseStockHandler.
 */
class ReleaseStockHandlerTest {

    private ReleaseStockHandler handler;
    private InventoryRepository inventoryRepository;
    private StoreId storeId;
    private ProductId productId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ReleaseStockHandler(inventoryRepository);
        storeId = StoreId.newId();
        productId = ProductId.newId();
        inventory = new Inventory(storeId, productId, 100, 50);
    }

    @Test
    void shouldReleaseStock() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), 20);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(30);
        assertThat(inventory.available()).isEqualTo(70);
        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), 20);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryNotFoundException.class);

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReleasingMoreThanReserved() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), 100);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot release more than reserved");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReleasingZeroQuantity() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), 0);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to release must be positive");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReleasingNegativeQuantity() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), -10);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to release must be positive");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldReleaseAllReservedStock() {
        // given
        ReleaseStockCommand command = new ReleaseStockCommand(storeId.id(), productId.id(), 50);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityReserved()).isZero();
        assertThat(inventory.available()).isEqualTo(100);
        verify(inventoryRepository, times(1)).save(inventory);
    }
}
