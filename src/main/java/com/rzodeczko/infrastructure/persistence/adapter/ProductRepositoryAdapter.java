package com.rzodeczko.infrastructure.persistence.adapter;

import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.repository.ProductRepository;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.infrastructure.persistence.mapper.ProductEntityMapper;
import com.rzodeczko.infrastructure.persistence.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductEntityMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return jpaProductRepository.findById(id.id()).map(mapper::toDomain);
    }
}
