package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.infrastructure.persistence.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemEntityMapperTest {

    private final OrderItemEntityMapper mapper = new OrderItemEntityMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        UUID productId = UUID.randomUUID();
        OrderItem domain = new OrderItem(new ProductId(productId), 5, new Money(BigDecimal.valueOf(19.99), Currency.getInstance("PLN")));

        // when
        OrderItemEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getProductId()).isEqualTo(productId);
        assertThat(entity.getQuantity()).isEqualTo(5);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo(BigDecimal.valueOf(19.99));
        assertThat(entity.getCurrency()).isEqualTo("PLN");
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        UUID productId = UUID.randomUUID();
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(productId)
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(49.99))
                .currency("PLN")
                .build();

        // when
        OrderItem domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getProductId().id()).isEqualTo(productId);
        assertThat(domain.getQuantity()).isEqualTo(3);
        assertThat(domain.getUnitPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(49.99));
        assertThat(domain.getUnitPrice().currency()).isEqualTo(Currency.getInstance("PLN"));
    }
}
