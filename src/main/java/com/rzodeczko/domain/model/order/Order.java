package com.rzodeczko.domain.model.order;

import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents an order aggregate in the domain model.
 * Manages order lifecycle, items, buyer details, and external service references.
 */
public class Order {
    /** The unique identifier of the order. */
    private final OrderId id;
    /** The store identifier where the order was placed. */
    private StoreId storeId;
    /** The current status of the order. */
    private OrderStatus status;
    /** The list of items in the order. */
    private final List<OrderItem> items = new ArrayList<>();
    /** The total amount of the order. */
    private Money totalAmount = Money.ZERO_PLN;

    /**
     * Buyer details snapshot at order placement time.
     * Order must be self-sufficient even if customer changes account data.
     */
    private String buyerEmail;
    private String buyerName;
    private String buyerTaxId;

    /**
     * External service references and metadata.
     * IDs are kept to link order with payment and invoice without additional queries.
     */
    private UUID paymentId;
    private UUID invoiceId;
    private String paymentRedirectUrl;
    private Instant awaitingPaymentSince;

    /**
     * Constructor for creating a new order.
     * @param id the order ID
     * @param storeId the store ID
     */
    public Order(OrderId id, StoreId storeId) {
        this.id = id;
        this.storeId = storeId;
        this.status = OrderStatus.DRAFT;
    }

    /**
     * Private constructor for reconstructing from persistence layer.
     */
    private Order(
            OrderId id,
            StoreId storeId,
            OrderStatus status,
            String buyerEmail,
            String buyerName,
            String buyerTaxId,
            UUID paymentId,
            UUID invoiceId,
            String paymentRedirectUrl,
            Instant awaitingPaymentSince
    ) {
        this.id = id;
        this.storeId = storeId;
        this.status = status;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.buyerTaxId = buyerTaxId;
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.paymentRedirectUrl = paymentRedirectUrl;
        this.awaitingPaymentSince = awaitingPaymentSince;
    }

    /**
     * Factory method for restoring aggregate from persistence.
     * No validation as data was validated on first save.
     * @param id the order ID
     * @param storeId the store ID
     * @param status the order status
     * @param buyerEmail the buyer email
     * @param buyerName the buyer name
     * @param buyerTaxId the buyer tax ID
     * @param paymentId the payment ID
     * @param invoiceId the invoice ID
     * @param paymentRedirectUrl the payment redirect URL
     * @param awaitingPaymentSince the timestamp when awaiting payment started
     * @return the restored Order instance
     */
    public static Order restore(
            OrderId id,
            StoreId storeId,
            OrderStatus status,
            String buyerEmail,
            String buyerName,
            String buyerTaxId,
            UUID paymentId,
            UUID invoiceId,
            String paymentRedirectUrl,
            Instant awaitingPaymentSince
    ) {
        return new Order(
                id,
                storeId,
                status,
                buyerEmail,
                buyerName,
                buyerTaxId,
                paymentId,
                invoiceId,
                paymentRedirectUrl,
                awaitingPaymentSince
        );
    }

    /**
     * Mutates order state, calculates business logic.
     */

    /**
     * Assigns buyer details to the order.
     * @param buyerEmail the buyer's email
     * @param buyerName the buyer's name
     * @param buyerTaxId the buyer's tax ID
     * @throws IllegalArgumentException if any parameter is null or blank
     */
    public void assignBuyerDetails(String buyerEmail, String buyerName, String buyerTaxId) {
        if (buyerEmail == null || buyerEmail.isBlank()) {
            throw new IllegalArgumentException("Buyer email is required");
        }

        if (buyerName == null || buyerName.isBlank()) {
            throw new IllegalArgumentException("Buyer name is required");
        }

        if (buyerTaxId == null || buyerTaxId.isBlank()) {
            throw new IllegalArgumentException("Buyer tax id is required");
        }

        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.buyerTaxId = buyerTaxId;
    }

    /**
     * Adds an item to the order.
     * @param item the order item to add
     */
    public void addItem(OrderItem item) {
        ensureDraft();
        items.add(item);
        recalculateTotal();
    }

    /**
     * Adds an item during restore - skips ensureDraft.
     * @param item the order item to add
     */
    public void addItemForRestore(OrderItem item) {
        items.add(item);
        recalculateTotal();
    }

    /**
     * Removes an item by product ID.
     * @param productId the product ID to remove
     * @throws IllegalArgumentException if item not found
     */
    public void removeItemByProductId(ProductId productId) {
        ensureDraft();
        boolean removed = items.removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new IllegalArgumentException(
                    "Item with product id " + productId + " not found in order");
        }
        recalculateTotal();
    }

    /**
     * Places the order.
     * @throws IllegalStateException if order has no items or buyer details missing
     */
    public void place() {
        ensureDraft();

        if (items.isEmpty()) {
            throw new IllegalStateException("Order cannot be placed without items");
        }

        if (buyerEmail == null || buyerName == null || buyerTaxId == null) {
            throw new IllegalStateException("Buyer details must be assigned before placing order");
        }

        this.status = OrderStatus.PLACED;
    }

    /**
     * Marks the order as awaiting payment.
     * Transition PLACED -> AWAITING_PAYMENT.
     * @param paymentId the payment ID
     * @param redirectUrl the payment redirect URL
     * @throws IllegalStateException if not in PLACED status
     * @throws IllegalArgumentException if paymentId is null
     */
    public void markAwaitingPayment(UUID paymentId, String redirectUrl) {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Only PLACED orders can be moved to AWAITING_PAYMENT"
            );
        }

        if (paymentId == null) {
            throw new IllegalArgumentException("Payment id is required");
        }

        this.paymentId = paymentId;
        this.paymentRedirectUrl = redirectUrl;
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.awaitingPaymentSince = Instant.now();
    }

    /**
     * Marks the order as paid.
     * Transition AWAITING_PAYMENT -> PAID.
     * @param confirmedPaymentId the confirmed payment ID
     * @throws IllegalStateException if not in AWAITING_PAYMENT status
     * @throws IllegalArgumentException if payment ID mismatch
     */
    public void markPaid(UUID confirmedPaymentId) {
        if (this.status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException(
                    "Only AWAITING_PAYMENT orders can be marked as PAID"
            );
        }

        if (!this.paymentId.equals(confirmedPaymentId)) {
            throw new IllegalArgumentException(
                    "Payment ID mismatch. Expected: %s, got: %s"
                            .formatted(paymentId, confirmedPaymentId)
            );
        }

        this.status = OrderStatus.PAID;
    }

    /**
     * Assigns an invoice to the order.
     * @param invoiceId the invoice ID
     * @throws IllegalStateException if not in PAID status
     */
    public void assignInvoice(UUID invoiceId) {
        if (this.status != OrderStatus.PAID) {
            throw new IllegalStateException(
                    "Invoice can only be assigned to PAID orders"
            );
        }
        this.invoiceId = invoiceId;
    }

    /**
     * Fulfills the order.
     * @throws IllegalStateException if not in PAID status
     */
    public void fulfill() {
        if (status != OrderStatus.PAID) {
            throw new IllegalStateException("Only PAID orders can be fulfilled");
        }
        this.status = OrderStatus.FULFILLED;
    }

    /**
     * Cancels the order.
     * @throws IllegalStateException if in invalid status for cancellation
     */
    public void cancel() {
        if (
                status != OrderStatus.PLACED &&
                        status != OrderStatus.AWAITING_PAYMENT &&
                        status != OrderStatus.PAID
        ) {
            throw new IllegalStateException(
                    "Order in status %s cannot be cancelled".formatted(status.name())
            );
        }
        this.status = OrderStatus.CANCELLED;
    }

    /**
     * Reverts to DRAFT after technical error at PLACED.
     * Not for AWAITING_PAYMENT - payment already initiated.
     * @throws IllegalStateException if not in PLACED status
     */
    public void revertToDraft() {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException(
                    "Only PLACED orders can be reverted to DRAFT, current: " + this.status
            );
        }

        this.status = OrderStatus.DRAFT;
        this.buyerEmail = null;
        this.buyerName = null;
        this.buyerTaxId = null;
    }

    /**
     * Clears payment data.
     * @throws IllegalStateException if not in DRAFT status
     */
    public void clearPaymentData() {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("clearPaymentData only allowed in DRAFT state");
        }
        this.paymentId = null;
        this.paymentRedirectUrl = null;
        this.buyerEmail = null;
        this.buyerName = null;
        this.buyerTaxId = null;
        this.awaitingPaymentSince = null;
    }

    /**
     * Relocates the order to a new store.
     * @param newStoreId the new store ID
     * @throws IllegalStateException if not in DRAFT status
     * @throws IllegalArgumentException if newStoreId is null
     */
    public void relocate(StoreId newStoreId) {
        if (!isDraft()) {
            throw new IllegalStateException("Only DRAFT orders can be relocated");
        }

        if (newStoreId == null) {
            throw new IllegalArgumentException("New store id cannot be null");
        }

        this.storeId = newStoreId;
    }

    /**
     * Predicate methods for checking order state.
     */

    /**
     * Checks if the order is in DRAFT status.
     * @return true if draft
     */
    public boolean isDraft() {
        return this.status == OrderStatus.DRAFT;
    }

    /**
     * Checks if the order is ready for fulfillment.
     * @return true if paid and has items
     */
    public boolean isReadyForFulfillment() {
        return this.status == OrderStatus.PAID && !items.isEmpty();
    }

    /**
     * Checks if the order is already paid with the given payment ID.
     * @param paymentId the payment ID to check
     * @return true if the order is PAID and has the same payment ID
     */
    public boolean isAlreadyPaidWith(UUID paymentId) {
        return this.status == OrderStatus.PAID && this.paymentId != null && this.paymentId.equals(paymentId);
    }

    /**
     * Getter methods for accessing order properties.
     */

    /**
     * Gets the order ID.
     * @return the order ID
     */
    public OrderId getId() {
        return id;
    }

    /**
     * Gets the store ID.
     * @return the store ID
     */
    public StoreId getStoreId() {
        return storeId;
    }

    /**
     * Gets the order status.
     * @return the status
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Gets the list of items.
     * @return unmodifiable list of items
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Gets the total amount.
     * @return the total amount
     */
    public Money getTotalAmount() {
        return totalAmount;
    }

    /**
     * Gets the buyer email.
     * @return the buyer email
     */
    public String getBuyerEmail() {
        return buyerEmail;
    }

    /**
     * Gets the buyer name.
     * @return the buyer name
     */
    public String getBuyerName() {
        return buyerName;
    }

    /**
     * Gets the buyer tax ID.
     * @return the buyer tax ID
     */
    public String getBuyerTaxId() {
        return buyerTaxId;
    }

    /**
     * Gets the payment ID.
     * @return the payment ID
     */
    public UUID getPaymentId() {
        return paymentId;
    }

    /**
     * Gets the invoice ID.
     * @return the invoice ID
     */
    public UUID getInvoiceId() {
        return invoiceId;
    }

    /**
     * Gets the payment redirect URL.
     * @return the redirect URL
     */
    public String getPaymentRedirectUrl() {
        return paymentRedirectUrl;
    }

    /**
     * Gets the timestamp when awaiting payment started.
     * @return the timestamp
     */
    public Instant getAwaitingPaymentSince() {
        return awaitingPaymentSince;
    }


    /**
     * Helper methods for internal order logic.
     */

    /**
     * Ensures the order is in DRAFT status.
     * @throws IllegalStateException if not draft
     */
    private void ensureDraft() {
        if (this.status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Can modify order only in DRAFT state");
        }
    }

    /**
     * Recalculates the total amount.
     */
    private void recalculateTotal() {
        this.totalAmount = items
                .stream()
                .map(OrderItem::lineTotal)
                .reduce(Money.ZERO_PLN, (a, b) -> a.add(b));
    }
}
