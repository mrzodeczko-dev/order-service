package com.rzodeczko.domain.model.outbox;

/**
 * Status of invoice outbox task.
 */
public enum InvoiceOutboxStatus {
    /** Waiting to be sent to invoice-service. */
    PENDING,
    /** Invoice issued, invoiceId saved in order. */
    SENT,
    /** Max retries exceeded - requires manual intervention. */
    FAILED
}
