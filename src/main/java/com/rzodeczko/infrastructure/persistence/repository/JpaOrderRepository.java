package com.rzodeczko.infrastructure.persistence.repository;

import com.rzodeczko.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query("select o from OrderEntity o left join fetch o.items where o.id = :id")
    Optional<OrderEntity> findByIdWithItems(@Param("id") UUID id);

    @Query("select o from OrderEntity o left join fetch o.items")
    List<OrderEntity> findAllWithItems();

    @Query("""
                select o from OrderEntity o left join fetch o.items
                where o.status = 'AWAITING_PAYMENT' and o.awaitingPaymentSince < :cutoff
            """)
    List<OrderEntity> findExpiredAwaitingPayment(@Param("cutoff") Instant cutoff);

    @Query("""
                select o from OrderEntity o left join fetch o.items
                where o.status = 'DRAFT' and o.paymentId is not null
                and o.awaitingPaymentSince < :cutoff
            """)
    List<OrderEntity> findDraftWithPaymentOlderThan(@Param("cutoff") Instant cutoff);

    @Query("""
                select coalesce(sum(oi.quantity), 0)
                from OrderEntity o join o.items oi
                where o.status = 'DRAFT' and oi.productId = :productId and o.storeId = :storeId
            """)
    int sumQuantityOfProductInDraftOrders(@Param("productId") UUID productId, @Param("storeId") UUID storeId);
}
