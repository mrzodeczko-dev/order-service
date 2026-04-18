package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReserveStockCommand;
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
 * Unit tests for ReserveStockHandler.
 */
class ReserveStockHandlerTest {

    private ReserveStockHandler handler;
    private InventoryRepository inventoryRepository;
    private StoreId storeId;
    private ProductId productId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ReserveStockHandler(inventoryRepository);
        storeId = StoreId.newId();
        productId = ProductId.newId();
        inventory = new Inventory(storeId, productId, 100, 10);
    }

    @Test
    void shouldReserveStock() {
        // given
        ReserveStockCommand command = new ReserveStockCommand(storeId.id(), productId.id(), 30);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(40);
        assertThat(inventory.available()).isEqualTo(60);
        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // given
        ReserveStockCommand command = new ReserveStockCommand(storeId.id(), productId.id(), 30);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inventory not found");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        // given
        ReserveStockCommand command = new ReserveStockCommand(storeId.id(), productId.id(), 100);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not enough stock to reserve");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReservingZeroQuantity() {
        // given
        ReserveStockCommand command = new ReserveStockCommand(storeId.id(), productId.id(), 0);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity to reserve must be positive");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldReservePartiallyAvailableStock() {
        // given
        ReserveStockCommand command = new ReserveStockCommand(storeId.id(), productId.id(), 50);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityReserved()).isEqualTo(60);
        assertThat(inventory.available()).isEqualTo(40);
        verify(inventoryRepository, times(1)).save(inventory);
    }
}

