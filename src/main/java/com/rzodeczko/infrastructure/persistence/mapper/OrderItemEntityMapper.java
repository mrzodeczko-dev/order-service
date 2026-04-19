package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class OrderItemEntityMapper {
    public OrderItemEntity toEntity(OrderItem domain) {
        return OrderItemEntity
                .builder()
                .productId(domain.getProductId().id())
                .quantity(domain.getQuantity())
                .unitPrice(domain.getUnitPrice().amount())
                .currency(domain.getUnitPrice().currency().getCurrencyCode())
                .build();
    }

    public OrderItem toDomain(OrderItemEntity entity) {
        return new OrderItem(
                new ProductId(entity.getProductId()),
                entity.getQuantity(),
                new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency()))
        );
    }
}
