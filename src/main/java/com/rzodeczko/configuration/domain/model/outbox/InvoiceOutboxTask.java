package com.rzodeczko.configuration.domain.model.outbox;

import java.time.Instant;
import java.util.UUID;

/**
 * Outbox task for generating invoices.
 * Solves the problem: order.markPaid() - db save, HTTP to invoice-service, order.assignInvoice() - crash here = invoice issued, no invoiceID in db.
 * Solution: markPaid() + INSERT invoice_outbox_tasks - one transaction (crash impossible).
 * Scheduler InvoiceOutboxProcessor every 5s: gets PENDING -> HTTP -> assignInvoice.
 * Error -> retryCount++ (status PENDING, scheduler retries).
 * retryCount >= MAX_RETRY -> status FAILED + alert in log.
 */
public class InvoiceOutboxTask {
    /** The task ID. */
    private final UUID id;
    /** The order ID. */
    private final UUID orderId;
    /** The task status. */
    private InvoiceOutboxStatus status;
    /** The retry count. */
    private int retryCount;
    /** The creation timestamp. */
    private final Instant createdAt;
    /** The processing timestamp. */
    private Instant processedAt;

    /** Maximum retry count. */
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * Creates a new InvoiceOutboxTask.
     * @param id the task ID
     * @param orderId the order ID
     * @param status the status
     * @param retryCount the retry count
     * @param createdAt the creation time
     * @param processedAt the processing time
     */
    public InvoiceOutboxTask(
            UUID id,
            UUID orderId,
            InvoiceOutboxStatus status,
            int retryCount,
            Instant createdAt,
            Instant processedAt
    ) {
        this.id = id;
        this.orderId = orderId;
        this.status = status;
        this.retryCount = retryCount;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    /**
     * Creates a new pending task for the order.
     * @param orderId the order ID
     * @return the new task
     */
    public static InvoiceOutboxTask create(UUID orderId) {
        return new InvoiceOutboxTask(
                UUID.randomUUID(),
                orderId,
                InvoiceOutboxStatus.PENDING,
                0,
                Instant.now(),
                null
        );
    }

    /**
     * Marks the task as sent.
     */
    public void markSent() {
        this.status = InvoiceOutboxStatus.SENT;
        this.processedAt = Instant.now();
    }

    /**
     * Marks the task as failed and increments retry count.
     * If max retries reached, sets status to FAILED.
     */
    public void markFailed() {
        ++this.retryCount;
        if (this.retryCount >= MAX_RETRY_COUNT) {
            this.status = InvoiceOutboxStatus.FAILED;
        }
    }

    /**
     * Checks if the task is exhausted (failed after max retries).
     * @return true if exhausted
     */
    public boolean isExhausted() {
        return this.status == InvoiceOutboxStatus.FAILED;
    }

    /**
     * Gets the task ID.
     * @return the ID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the order ID.
     * @return the order ID
     */
    public UUID getOrderId() {
        return orderId;
    }

    /**
     * Gets the status.
     * @return the status
     */
    public InvoiceOutboxStatus getStatus() {
        return status;
    }

    /**
     * Gets the retry count.
     * @return the retry count
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * Gets the creation timestamp.
     * @return the creation time
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the processing timestamp.
     * @return the processing time
     */
    public Instant getProcessedAt() {
        return processedAt;
    }
}
