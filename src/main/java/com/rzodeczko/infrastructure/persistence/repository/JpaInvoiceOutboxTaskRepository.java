package com.rzodeczko.infrastructure.persistence.repository;


import com.rzodeczko.infrastructure.persistence.entity.InvoiceOutboxTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaInvoiceOutboxTaskRepository extends JpaRepository<InvoiceOutboxTaskEntity, UUID> {
    List<InvoiceOutboxTaskEntity> findAllByStatus(String status);

    Optional<InvoiceOutboxTaskEntity> findByOrderId(UUID orderId);
}
