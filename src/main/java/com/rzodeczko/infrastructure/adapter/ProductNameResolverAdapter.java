package com.rzodeczko.infrastructure.adapter;

import com.rzodeczko.application.port.ProductNameResolver;
import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.repository.ProductRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductNameResolverAdapter implements ProductNameResolver {

    private final ProductRepository productRepository;

    @Override
    public String resolve(ProductId productId) {
        return productRepository
                .findById(productId)
                .map(Product::getName)
                .orElse("Unknown product ( " + productId.id() + " )");
    }
}
