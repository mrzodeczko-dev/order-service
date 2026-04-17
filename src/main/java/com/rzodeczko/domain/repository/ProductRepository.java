package com.rzodeczko.domain.repository;

import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.valueobject.ProductId;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(ProductId id);
}
