package com.rzodeczko.infrastructure.outbox;

import com.rzodeczko.domain.model.outbox.InvoiceOutboxStatus;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class InvoiceOutboxProcessorTest {

    private InvoiceOutboxProcessor processor;
    private InvoiceOutboxTaskRepository repository;
    private InvoiceOutboxSender sender;

    @BeforeEach
    void setUp() {
        repository = mock(InvoiceOutboxTaskRepository.class);
        sender = mock(InvoiceOutboxSender.class);
        processor = new InvoiceOutboxProcessor(repository, sender);
    }

    @Test
    void shouldProcessAllPendingTasks() {
        // given
        InvoiceOutboxTask task1 = new InvoiceOutboxTask(UUID.randomUUID(), UUID.randomUUID(), InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);
        InvoiceOutboxTask task2 = new InvoiceOutboxTask(UUID.randomUUID(), UUID.randomUUID(), InvoiceOutboxStatus.PENDING, 0, Instant.now(), null);
        when(repository.findAllPending()).thenReturn(List.of(task1, task2));

        // when
        processor.process();

        // then
        verify(sender).send(task1);
        verify(sender).send(task2);
    }

    @Test
    void shouldDoNothingWhenNoPendingTasks() {
        // given
        when(repository.findAllPending()).thenReturn(Collections.emptyList());

        // when
        processor.process();

        // then
        verifyNoInteractions(sender);
    }
}
