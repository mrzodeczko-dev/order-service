package com.rzodeczko.domain.model.inventory;



import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents inventory for a product in a store.
 * ID is domain field for adapter to save UPDATE without SELECT.
 * Version for optimistic locking.
 * UUID id instead of InventoryId: conscious decision - ID is internal key, not business identity.
 */
public class Inventory {
    /** Internal ID. */
    private final UUID id;
    /** Store ID. */
    private final StoreId storeId;
    /** Product ID. */
    private final ProductId productId;
    /** Quantity on hand. */
    private int quantityOnHand;
    /** Quantity reserved. */
    private int quantityReserved;
    /** Version for optimistic locking. */
    private final Long version;

    /**
     * Constructor for new records - ID generated here, version null.
     * @param storeId the store ID
     * @param productId the product ID
     * @param quantityOnHand quantity on hand
     * @param quantityReserved quantity reserved
     */
    public Inventory(StoreId storeId, ProductId productId, int quantityOnHand, int quantityReserved) {
        this.id = UUID.randomUUID();
        this.storeId = Objects.requireNonNull(storeId, "StoreId cannot be null");
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        this.quantityOnHand = Math.max(0, quantityOnHand);
        this.quantityReserved = Math.max(0, quantityReserved);
        this.version = null;
    }

    /**
     * Constructor for reconstruction from DB - carries ID and version.
     * @param id the ID
     * @param storeId the store ID
     * @param productId the product ID
     * @param quantityOnHand quantity on hand
     * @param quantityReserved quantity reserved
     * @param version the version
     */
    public Inventory(
            UUID id,
            StoreId storeId,
            ProductId productId,
            int quantityOnHand,
            int quantityReserved,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "Id cannot be null");
        this.storeId = Objects.requireNonNull(storeId, "StoreId cannot be null");
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        this.quantityOnHand = Math.max(0, quantityOnHand);
        this.quantityReserved = Math.max(0, quantityReserved);
        this.version = version;
    }

    /**
     * Calculates available quantity.
     * @return available quantity
     */
    public int available() {
        return Math.max(0, quantityOnHand - quantityReserved);
    }

    /**
     * Reserves quantity.
     * @param quantity quantity to reserve
     * @throws IllegalArgumentException if not enough stock
     */
    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be positive");
        }

        if (quantity > available()) {
            throw new IllegalArgumentException("Not enough stock to reserve");
        }

        quantityReserved += quantity;
    }

    /**
     * Releases reserved quantity.
     * @param quantity quantity to release
     * @throws IllegalArgumentException if releasing more than reserved
     */
    public void release(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to release must be positive");
        }

        if (quantity > quantityReserved) {
            throw new IllegalArgumentException("Cannot release more than reserved");
        }

        quantityReserved -= quantity;
    }

    /**
     * Replenishes stock.
     * @param quantity quantity to add
     */
    public void replenish(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        quantityOnHand += quantity;
    }

    /**
     * Updates quantity when fulfilled - releases reservation and decreases physical stock.
     * @param quantity quantity fulfilled
     */
    public void updateQuantityWhenFulfilled(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        release(quantity);
        this.quantityOnHand -= quantity;
    }

    /**
     * Gets the ID.
     * @return the ID
     */
    public UUID getId() {
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
     * Gets the product ID.
     * @return the product ID
     */
    public ProductId getProductId() {
        return productId;
    }

    /**
     * Gets quantity on hand.
     * @return quantity on hand
     */
    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    /**
     * Gets quantity reserved.
     * @return quantity reserved
     */
    public int getQuantityReserved() {
        return quantityReserved;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public Long getVersion() {
        return version;
    }
}
