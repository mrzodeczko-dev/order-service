package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.infrastructure.persistence.entity.ProductEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEntityMapperTest {

    private final ProductEntityMapper mapper = new ProductEntityMapper();

    @Test
    void shouldMapEntityToDomain() {
        // given
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder()
                .id(id)
                .sku("SKU-001")
                .name("Widget")
                .unitPrice(BigDecimal.valueOf(29.99))
                .currency("PLN")
                .taxRate(BigDecimal.valueOf(0.23))
                .active(true)
                .build();

        // when
        Product domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId().id()).isEqualTo(id);
        assertThat(domain.getSku()).isEqualTo("SKU-001");
        assertThat(domain.getName()).isEqualTo("Widget");
        assertThat(domain.getUnitPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
        assertThat(domain.getUnitPrice().currency().getCurrencyCode()).isEqualTo("PLN");
        assertThat(domain.getTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.23));
        assertThat(domain.isActive()).isTrue();
    }

    @Test
    void shouldMapInactiveProduct() {
        // given
        ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .sku("SKU-OFF")
                .name("Discontinued")
                .unitPrice(BigDecimal.ONE)
                .currency("PLN")
                .taxRate(BigDecimal.ZERO)
                .active(false)
                .build();

        // when
        Product domain = mapper.toDomain(entity);

        // then
        assertThat(domain.isActive()).isFalse();
    }
}
