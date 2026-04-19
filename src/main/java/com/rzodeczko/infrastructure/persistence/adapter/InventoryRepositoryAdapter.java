package com.rzodeczko.infrastructure.persistence.adapter;


import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.mapper.InventoryEntityMapper;
import com.rzodeczko.infrastructure.persistence.repository.JpaInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {

    private final JpaInventoryRepository jpaInventoryRepository;
    private final InventoryEntityMapper mapper;

    @Override
    @Transactional
    public void save(Inventory inventory) {
        jpaInventoryRepository.save(mapper.toEntity(inventory));
    }

    @Override
    @Transactional
    public void saveAll(List<Inventory> inventories) {
        inventories.forEach(this::save);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Inventory> findByStoreAndProduct(StoreId storeId, ProductId productId) {
        return jpaInventoryRepository
                .findByStoreIdAndProductId(storeId.id(), productId.id())
                .map(mapper::toDomain);
    }
}
