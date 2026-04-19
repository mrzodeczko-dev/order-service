package com.rzodeczko.infrastructure.persistence.adapter;

import com.rzodeczko.domain.model.outbox.InvoiceOutboxStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import com.rzodeczko.infrastructure.persistence.mapper.InvoiceOutboxTaskMapper;
import com.rzodeczko.infrastructure.persistence.repository.JpaInvoiceOutboxTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvoiceOutboxTaskRepositoryAdapter implements InvoiceOutboxTaskRepository {

    private final JpaInvoiceOutboxTaskRepository jpaInvoiceOutboxTaskRepository;
    private final InvoiceOutboxTaskMapper mapper;

    @Override
    @Transactional
    public void save(InvoiceOutboxTask task) {
        jpaInvoiceOutboxTaskRepository
                .findById(task.getId())
                .map(existing -> {
                    existing.setStatus(task.getStatus().name());
                    existing.setRetryCount(task.getRetryCount());
                    existing.setProcessedAt(task.getProcessedAt());
                    return existing;
                })
                .orElseGet(() -> jpaInvoiceOutboxTaskRepository.save(mapper.toEntity(task)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceOutboxTask> findAllPending() {
        return jpaInvoiceOutboxTaskRepository
                .findAllByStatus(InvoiceOutboxStatus.PENDING.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InvoiceOutboxTask> findByOrderId(UUID orderId) {
        return jpaInvoiceOutboxTaskRepository
                .findByOrderId(orderId)
                .map(mapper::toDomain);
    }
}
