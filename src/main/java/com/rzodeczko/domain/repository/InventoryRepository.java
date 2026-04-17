package com.rzodeczko.domain.repository;

import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository {
    void save(Inventory inventory);
    void saveAll(List<Inventory> inventories);
    Optional<Inventory> findByStoreAndProduct(StoreId storeId, ProductId productId);
}
