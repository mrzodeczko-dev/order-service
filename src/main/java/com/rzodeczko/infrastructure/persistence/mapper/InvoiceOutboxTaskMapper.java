package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.outbox.InvoiceOutboxStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.infrastructure.persistence.entity.InvoiceOutboxTaskEntity;
import org.springframework.stereotype.Component;

@Component
public class InvoiceOutboxTaskMapper {
    public InvoiceOutboxTaskEntity toEntity(InvoiceOutboxTask domain) {
        return InvoiceOutboxTaskEntity
                .builder()
                .id(domain.getId())
                .orderId(domain.getOrderId())
                .status(domain.getStatus().name())
                .retryCount(domain.getRetryCount())
                .createdAt(domain.getCreatedAt())
                .processedAt(domain.getProcessedAt())
                .build();
    }

    public InvoiceOutboxTask toDomain(InvoiceOutboxTaskEntity entity) {
        return new InvoiceOutboxTask(
                entity.getId(),
                entity.getOrderId(),
                InvoiceOutboxStatus.valueOf(entity.getStatus()),
                entity.getRetryCount(),
                entity.getCreatedAt(),
                entity.getProcessedAt()
        );
    }
}
