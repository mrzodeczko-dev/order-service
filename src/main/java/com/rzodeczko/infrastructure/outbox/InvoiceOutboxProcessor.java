package com.rzodeczko.infrastructure.outbox;

import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceOutboxProcessor {
    private final InvoiceOutboxTaskRepository invoiceOutboxTaskRepository;
    private final InvoiceOutboxSender invoiceOutboxSender;

    @Scheduled(fixedDelayString = "${scheduler.invoice-outbox.interval-ms}")
    @SchedulerLock(name = "invoice_outbox_processor", lockAtMostFor = "30s", lockAtLeastFor = "5s")
    public void process() {
        List<InvoiceOutboxTask> pendingTasks = invoiceOutboxTaskRepository.findAllPending();
        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("InvoiceOutboxProcessor: processing {} pending task(s)", pendingTasks.size());
        for (InvoiceOutboxTask task : pendingTasks) {
            invoiceOutboxSender.send(task);
        }
        log.info("InvoiceOutboxProcessor: batch completed");
    }
}
