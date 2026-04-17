package com.rzodeczko.domain.model.outbox;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for InvoiceOutboxTask.
 */
class InvoiceOutboxTaskTest {

    @Test
    void shouldCreateTaskWithPendingStatus() {
        // given
        UUID orderId = UUID.randomUUID();

        // when
        InvoiceOutboxTask task = InvoiceOutboxTask.create(orderId);

        // then
        assertThat(task.getOrderId()).isEqualTo(orderId);
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.PENDING);
        assertThat(task.getRetryCount()).isZero();
        assertThat(task.getCreatedAt()).isNotNull();
        assertThat(task.getProcessedAt()).isNull();
        assertThat(task.getId()).isNotNull();
    }

    @Test
    void shouldMarkTaskAsSent() {
        // given
        InvoiceOutboxTask task = InvoiceOutboxTask.create(UUID.randomUUID());

        // when
        task.markSent();

        // then
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.SENT);
        assertThat(task.getProcessedAt()).isNotNull();
    }

    @Test
    void shouldMarkTaskAsFailedAndIncrementRetryCount() {
        // given
        InvoiceOutboxTask task = InvoiceOutboxTask.create(UUID.randomUUID());

        // when
        task.markFailed();

        // then
        assertThat(task.getRetryCount()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.PENDING);
    }

    @Test
    void shouldMarkTaskAsFailedWhenMaxRetriesReached() {
        // given
        InvoiceOutboxTask task = InvoiceOutboxTask.create(UUID.randomUUID());

        // when
        for (int i = 0; i < 5; i++) {
            task.markFailed();
        }

        // then
        assertThat(task.getRetryCount()).isEqualTo(5);
        assertThat(task.getStatus()).isEqualTo(InvoiceOutboxStatus.FAILED);
    }

    @Test
    void shouldCheckIfTaskIsExhausted() {
        // given
        InvoiceOutboxTask task = InvoiceOutboxTask.create(UUID.randomUUID());

        // when
        for (int i = 0; i < 5; i++) {
            task.markFailed();
        }

        // then
        assertThat(task.isExhausted()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenTaskIsNotExhausted() {
        // given
        InvoiceOutboxTask task = InvoiceOutboxTask.create(UUID.randomUUID());

        // when & then
        assertThat(task.isExhausted()).isFalse();
    }
}

