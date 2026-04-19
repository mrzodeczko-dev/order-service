package com.rzodeczko.infrastructure.persistence.repository;


import com.rzodeczko.infrastructure.persistence.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Optional<InventoryEntity> findByStoreIdAndProductId(UUID storeId, UUID productId);
}
