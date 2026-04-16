package com.rzodeczko.configuration.domain.model.order;

/**
 * Order lifecycle:
 *
 * DRAFT → PLACED → AWAITING_PAYMENT → PAID → FULFILLED
 *           ↓              ↓              ↓
 *        CANCELLED      CANCELLED     CANCELLED
 *
 * DRAFT            — cart, user adds products
 * PLACED           — "Order and pay", buyer details assigned
 * AWAITING_PAYMENT — payment initiated in payment system, waiting for webhook
 * PAID             — payment confirmed, invoice in outbox queue
 * FULFILLED        — order shipped, inventory updated
 * CANCELLED        — cancelled by user or scheduler timeout
 */
public enum OrderStatus {
    /** Cart, user adds products. */
    DRAFT,
    /** "Order and pay", buyer details assigned. */
    PLACED,
    /** Payment initiated, waiting for webhook. */
    AWAITING_PAYMENT,
    /** Payment confirmed, invoice queued. */
    PAID,
    /** Order shipped, inventory updated. */
    FULFILLED,
    /** Cancelled by user or timeout. */
    CANCELLED
}
