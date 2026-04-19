package com.rzodeczko.infrastructure.persistence.mapper;


import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.infrastructure.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

@Component
public class ProductEntityMapper {
    public Product toDomain(ProductEntity entity) {
        return new Product(
                new ProductId(entity.getId()),
                entity.getSku(),
                entity.getName(),
                new Money(entity.getUnitPrice(), Currency.getInstance(entity.getCurrency())),
                entity.isActive()
        );
    }
}
