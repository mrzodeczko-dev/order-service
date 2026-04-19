package com.rzodeczko.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(
        ExpireAwaitingPayment expireAwaitingPayment,
        InvoiceOutbox invoiceOutbox,
        Reconciliation reconciliation
) {
    public record ExpireAwaitingPayment(long intervalMs, long cutoff) {}
    public record InvoiceOutbox(long intervalMs) {}
    public record Reconciliation(long intervalMs, long paymentAgeMinutes) {}
}
