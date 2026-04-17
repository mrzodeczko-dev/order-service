package com.rzodeczko.domain.repository;



import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceOutboxTaskRepository {
    void save(InvoiceOutboxTask task);
    List<InvoiceOutboxTask> findAllPending();
    Optional<InvoiceOutboxTask> findByOrderId(UUID orderId);
}
