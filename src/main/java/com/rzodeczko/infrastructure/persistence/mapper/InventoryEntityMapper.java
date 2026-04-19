package com.rzodeczko.infrastructure.persistence.mapper;


import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.entity.InventoryEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryEntityMapper {
    public InventoryEntity toEntity(Inventory domain) {
        return InventoryEntity
                .builder()
                .id(domain.getId())
                .storeId(domain.getStoreId().id())
                .productId(domain.getProductId().id())
                .quantityOnHand(domain.getQuantityOnHand())
                .quantityReserved(domain.getQuantityReserved())
                .version(domain.getVersion())
                .build();
    }

    public Inventory toDomain(InventoryEntity entity) {
        return new Inventory(
                entity.getId(),
                new StoreId(entity.getStoreId()),
                new ProductId(entity.getProductId()),
                entity.getQuantityOnHand(),
                entity.getQuantityReserved(),
                entity.getVersion()
        );
    }
}
