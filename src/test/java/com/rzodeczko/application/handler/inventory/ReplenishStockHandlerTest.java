package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.ReplenishStockCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReplenishStockHandler.
 */
class ReplenishStockHandlerTest {

    private ReplenishStockHandler handler;
    private InventoryRepository inventoryRepository;
    private StoreId storeId;
    private ProductId productId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ReplenishStockHandler(inventoryRepository);
        storeId = StoreId.newId();
        productId = ProductId.newId();
        inventory = new Inventory(storeId, productId, 100, 10);
    }

    @Test
    void shouldReplenishStock() {
        // given
        ReplenishStockCommand command = new ReplenishStockCommand(storeId.id(), productId.id(), 50);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityOnHand()).isEqualTo(150);
        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, times(1)).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // given
        ReplenishStockCommand command = new ReplenishStockCommand(storeId.id(), productId.id(), 50);

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
    void shouldReplenishMultipleTimes() {
        // given
        ReplenishStockCommand command1 = new ReplenishStockCommand(storeId.id(), productId.id(), 25);
        ReplenishStockCommand command2 = new ReplenishStockCommand(storeId.id(), productId.id(), 30);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command1);
        handler.handle(command2);

        // then
        assertThat(inventory.getQuantityOnHand()).isEqualTo(155);
        verify(inventoryRepository, times(2)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, times(2)).save(inventory);
    }

    @Test
    void shouldThrowExceptionWhenReplenishingWithZeroQuantity() {
        // given
        ReplenishStockCommand command = new ReplenishStockCommand(storeId.id(), productId.id(), 0);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenReplenishingWithNegativeQuantity() {
        // given
        ReplenishStockCommand command = new ReplenishStockCommand(storeId.id(), productId.id(), -10);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be positive");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shouldSaveInventoryAfterReplenish() {
        // given
        ReplenishStockCommand command = new ReplenishStockCommand(storeId.id(), productId.id(), 100);

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        handler.handle(command);

        // then
        assertThat(inventory.getQuantityOnHand()).isEqualTo(200);
        verify(inventoryRepository, times(1)).save(inventory);
    }
}

