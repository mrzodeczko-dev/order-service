package com.rzodeczko.infrastructure.configuration;


import com.rzodeczko.infrastructure.persistence.entity.InventoryEntity;
import com.rzodeczko.infrastructure.persistence.entity.ProductEntity;
import com.rzodeczko.infrastructure.persistence.repository.JpaInventoryRepository;
import com.rzodeczko.infrastructure.persistence.repository.JpaProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!prod")
public class DataInitializer {
    private final JpaProductRepository jpaProductRepository;
    private final JpaInventoryRepository jpaInventoryRepository;

    @PostConstruct
    public void initData() {
        if (jpaInventoryRepository.count() > 0) {
            log.info("Products already exists, skipping initialization");
            return;
        }

        UUID store1Id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID store2Id = UUID.fromString("11111111-1111-1111-1111-111111111122");
        String PLN = Currency.getInstance("PLN").getCurrencyCode();

        var products = jpaProductRepository.saveAll(List.of(
                ProductEntity
                        .builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111112"))
                        .sku("LAPTOP-001")
                        .name("Laptop Dell XPS 13")
                        .unitPrice(BigDecimal.valueOf(5999))
                        .currency(PLN)
                        .active(true)
                        .build(),
                ProductEntity
                        .builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111113"))
                        .sku("PHONE-001")
                        .name("iPhone 17 Pro")
                        .unitPrice(BigDecimal.valueOf(6999))
                        .currency(PLN)
                        .active(true)
                        .build(),
                ProductEntity
                        .builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111114"))
                        .sku("HEADPHONES-001")
                        .name("Sony WH-1000XM5")
                        .unitPrice(BigDecimal.valueOf(1499))
                        .currency(PLN)
                        .active(true)
                        .build()
        ));

        for (var product : products) {
            for (UUID storeId : List.of(store1Id, store2Id)) {
                jpaInventoryRepository.save(InventoryEntity
                        .builder()
                        .id(UUID.randomUUID())
                        .storeId(storeId)
                        .productId(product.getId())
                        .quantityOnHand(50)
                        .quantityReserved(0)
                        .build());
            }
        }

        log.info("DataInitializer: inserted {} products x 2 stores", products.size());
    }
}
