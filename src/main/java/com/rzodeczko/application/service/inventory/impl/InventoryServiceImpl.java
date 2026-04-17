package com.rzodeczko.application.service.inventory.impl;

import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.application.command.inventory.ReplenishStockCommand;
import com.rzodeczko.application.command.inventory.ReserveStockCommand;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.inventory.ReplenishStockHandler;
import com.rzodeczko.application.handler.inventory.ReserveStockHandler;
import com.rzodeczko.application.service.inventory.InventoryService;

import java.util.UUID;

public class InventoryServiceImpl implements InventoryService {

    private final ReserveStockHandler reserveStockHandler;
    private final ReleaseStockHandler releaseStockHandler;
    private final ReplenishStockHandler replenishStockHandler;

    public InventoryServiceImpl(
            ReserveStockHandler reserveStockHandler,
            ReleaseStockHandler releaseStockHandler,
            ReplenishStockHandler replenishStockHandler) {
        this.reserveStockHandler = reserveStockHandler;
        this.releaseStockHandler = releaseStockHandler;
        this.replenishStockHandler = replenishStockHandler;
    }

    @Override
    public void reserve(UUID storeId, UUID productId, int quantity) {
        reserveStockHandler.handle(new ReserveStockCommand(storeId, productId, quantity));
    }

    @Override
    public void release(UUID storeId, UUID productId, int quantity) {
        releaseStockHandler.handle(new ReleaseStockCommand(storeId, productId, quantity));
    }

    @Override
    public void replenish(UUID storeId, UUID productId, int quantity) {
        replenishStockHandler.handle(new ReplenishStockCommand(storeId, productId, quantity));
    }
}
