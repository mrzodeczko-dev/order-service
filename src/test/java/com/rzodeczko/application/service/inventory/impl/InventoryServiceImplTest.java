package com.rzodeczko.application.service.inventory.impl;

import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.application.command.inventory.ReplenishStockCommand;
import com.rzodeczko.application.command.inventory.ReserveStockCommand;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.inventory.ReplenishStockHandler;
import com.rzodeczko.application.handler.inventory.ReserveStockHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InventoryServiceImplTest {

    private InventoryServiceImpl service;
    private ReserveStockHandler reserveStockHandler;
    private ReleaseStockHandler releaseStockHandler;
    private ReplenishStockHandler replenishStockHandler;

    private final UUID storeId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        reserveStockHandler = mock(ReserveStockHandler.class);
        releaseStockHandler = mock(ReleaseStockHandler.class);
        replenishStockHandler = mock(ReplenishStockHandler.class);
        service = new InventoryServiceImpl(reserveStockHandler, releaseStockHandler, replenishStockHandler);
    }

    @Test
    void shouldDelegateReserveToHandler() {
        // given
        int quantity = 5;

        // when
        service.reserve(storeId, productId, quantity);

        // then
        verify(reserveStockHandler).handle(new ReserveStockCommand(storeId, productId, quantity));
    }

    @Test
    void shouldDelegateReleaseToHandler() {
        // given
        int quantity = 3;

        // when
        service.release(storeId, productId, quantity);

        // then
        verify(releaseStockHandler).handle(new ReleaseStockCommand(storeId, productId, quantity));
    }

    @Test
    void shouldDelegateReplenishToHandler() {
        // given
        int quantity = 10;

        // when
        service.replenish(storeId, productId, quantity);

        // then
        verify(replenishStockHandler).handle(new ReplenishStockCommand(storeId, productId, quantity));
    }
}
