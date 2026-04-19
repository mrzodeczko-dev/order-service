package com.rzodeczko.application.port;

import com.rzodeczko.application.port.data.InvoiceItemData;

import java.util.List;
import java.util.UUID;

/**
 * Port for invoice service integration.
 */
public interface InvoicePort {
    /**
     * Generates an invoice for an order.
     * @param orderId the order ID
     * @param taxId the buyer's tax ID
     * @param buyerName the buyer's name
     * @param items the invoice items
     * @return the invoice ID
     */
    UUID generateInvoice(UUID orderId, String taxId, String buyerName, List<InvoiceItemData> items);

    /**
     * Deletes an invoice.
     * @param invoiceId the invoice ID
     */
    void deleteInvoice(UUID invoiceId);

    /**
     * Data transfer object for invoice items.
     */
}
