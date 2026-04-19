package com.rzodeczko.infrastructure.persistence.adapter;


import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.mapper.OrderEntityMapper;
import com.rzodeczko.infrastructure.persistence.repository.JpaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final OrderEntityMapper mapper;

    @Override
    @Transactional
    public void save(Order order) {
        jpaOrderRepository.saveAndFlush(mapper.toEntity(order));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId id) {
        return jpaOrderRepository.findByIdWithItems(id.id()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return jpaOrderRepository.findAllWithItems().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAwaitingPaymentOlderThan(Instant cutoff) {
        return jpaOrderRepository
                .findExpiredAwaitingPayment(cutoff)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findDraftWithPaymentOlderThan(Instant cutoff) {
        return jpaOrderRepository
                .findDraftWithPaymentOlderThan(cutoff)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int sumQuantityOfProductInDraftOrders(ProductId productId, StoreId storeId) {
        return jpaOrderRepository
                .sumQuantityOfProductInDraftOrders(productId.id(), storeId.id());
    }
}
