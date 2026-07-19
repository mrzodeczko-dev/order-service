package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.entity.InventoryEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryEntityMapperTest {

    private final InventoryEntityMapper mapper = new InventoryEntityMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        UUID id = UUID.randomUUID();
        UUID storeIdVal = UUID.randomUUID();
        UUID productIdVal = UUID.randomUUID();
        Inventory domain = new Inventory(id, new StoreId(storeIdVal), new ProductId(productIdVal), 100, 20, 5L);

        // when
        InventoryEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getStoreId()).isEqualTo(storeIdVal);
        assertThat(entity.getProductId()).isEqualTo(productIdVal);
        assertThat(entity.getQuantityOnHand()).isEqualTo(100);
        assertThat(entity.getQuantityReserved()).isEqualTo(20);
        assertThat(entity.getVersion()).isEqualTo(5L);
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        UUID id = UUID.randomUUID();
        UUID storeIdVal = UUID.randomUUID();
        UUID productIdVal = UUID.randomUUID();
        InventoryEntity entity = InventoryEntity.builder()
                .id(id)
                .storeId(storeIdVal)
                .productId(productIdVal)
                .quantityOnHand(50)
                .quantityReserved(10)
                .version(3L)
                .build();

        // when
        Inventory domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getStoreId().id()).isEqualTo(storeIdVal);
        assertThat(domain.getProductId().id()).isEqualTo(productIdVal);
        assertThat(domain.getQuantityOnHand()).isEqualTo(50);
        assertThat(domain.getQuantityReserved()).isEqualTo(10);
        assertThat(domain.getVersion()).isEqualTo(3L);
    }
}
