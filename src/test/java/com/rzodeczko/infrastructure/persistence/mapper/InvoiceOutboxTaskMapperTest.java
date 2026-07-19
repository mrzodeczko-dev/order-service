package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.outbox.InvoiceOutboxStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.infrastructure.persistence.entity.InvoiceOutboxTaskEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceOutboxTaskMapperTest {

    private final InvoiceOutboxTaskMapper mapper = new InvoiceOutboxTaskMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(60);
        Instant processedAt = Instant.now();
        InvoiceOutboxTask domain = new InvoiceOutboxTask(id, orderId, InvoiceOutboxStatus.SENT, 2, createdAt, processedAt);

        // when
        InvoiceOutboxTaskEntity entity = mapper.toEntity(domain);

        // then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getOrderId()).isEqualTo(orderId);
        assertThat(entity.getStatus()).isEqualTo("SENT");
        assertThat(entity.getRetryCount()).isEqualTo(2);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
        assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(120);
        InvoiceOutboxTaskEntity entity = InvoiceOutboxTaskEntity.builder()
                .id(id)
                .orderId(orderId)
                .status("PENDING")
                .retryCount(0)
                .createdAt(createdAt)
                .processedAt(null)
                .build();

        // when
        InvoiceOutboxTask domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getOrderId()).isEqualTo(orderId);
        assertThat(domain.getStatus()).isEqualTo(InvoiceOutboxStatus.PENDING);
        assertThat(domain.getRetryCount()).isZero();
        assertThat(domain.getCreatedAt()).isEqualTo(createdAt);
        assertThat(domain.getProcessedAt()).isNull();
    }

    @Test
    void shouldMapFailedTaskRoundTrip() {
        // given
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(300);
        Instant processedAt = Instant.now();
        InvoiceOutboxTask original = new InvoiceOutboxTask(id, orderId, InvoiceOutboxStatus.FAILED, 5, createdAt, processedAt);

        // when
        InvoiceOutboxTaskEntity entity = mapper.toEntity(original);
        InvoiceOutboxTask restored = mapper.toDomain(entity);

        // then
        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getOrderId()).isEqualTo(original.getOrderId());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getRetryCount()).isEqualTo(original.getRetryCount());
    }
}
