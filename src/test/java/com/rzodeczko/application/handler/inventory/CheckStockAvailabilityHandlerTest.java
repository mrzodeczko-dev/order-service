package com.rzodeczko.application.handler.inventory;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CheckStockAvailabilityHandler.
 */
class CheckStockAvailabilityHandlerTest {

    private CheckStockAvailabilityHandler handler;
    private InventoryRepository inventoryRepository;
    private OrderRepository orderRepository;
    private StoreId storeId;
    private ProductId productId;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventoryRepository = mock(InventoryRepository.class);
        orderRepository = mock(OrderRepository.class);
        handler = new CheckStockAvailabilityHandler(inventoryRepository, orderRepository);
        storeId = StoreId.newId();
        productId = ProductId.newId();
        inventory = new Inventory(storeId, productId, 100, 20);
    }

    @Test
    void shouldPassStockAvailabilityCheck() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 50
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId))
                .thenReturn(10);

        // when
        handler.handle(command);

        // then - available: 80 (100 - 20), real available: 70 (80 - 10), requested: 50
        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(orderRepository, times(1)).sumQuantityOfProductInDraftOrders(productId, storeId);
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 50
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Inventory not found");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(orderRepository, never()).sumQuantityOfProductInDraftOrders(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenRequestedQuantityExceedsAvailable() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 100
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId))
                .thenReturn(0);

        // when & then - available: 80 (100 - 20), real available: 80, requested: 100
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock for product")
                .hasMessageContaining("Requested: 100")
                .hasMessageContaining("available: 80");

        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
        verify(orderRepository, times(1)).sumQuantityOfProductInDraftOrders(productId, storeId);
    }

    @Test
    void shouldConsiderDraftOrdersInAvailabilityCheck() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 60
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId))
                .thenReturn(30);

        // when & then - available: 80 (100 - 20), real available: 50 (80 - 30), requested: 60
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough stock for product")
                .hasMessageContaining("Requested: 60")
                .hasMessageContaining("available: 50");

        verify(orderRepository, times(1)).sumQuantityOfProductInDraftOrders(productId, storeId);
    }

    @Test
    void shouldPassWhenRequestedEqualsRealAvailable() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 70
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId))
                .thenReturn(10);

        // when & then - available: 80 (100 - 20), real available: 70 (80 - 10), requested: 70
        handler.handle(command);
        verify(inventoryRepository, times(1)).findByStoreAndProduct(storeId, productId);
    }

    @Test
    void shouldHandleNoDraftOrders() {
        // given
        CheckStockAvailabilityCommand command = new CheckStockAvailabilityCommand(
                storeId.id(), productId.id(), 80
        );

        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));
        when(orderRepository.sumQuantityOfProductInDraftOrders(productId, storeId))
                .thenReturn(0);

        // when & then - available: 80 (100 - 20), real available: 80 (80 - 0), requested: 80
        handler.handle(command);
        verify(orderRepository, times(1)).sumQuantityOfProductInDraftOrders(productId, storeId);
    }
}

