package com.rzodeczko.configuration.domain.model.product;



import com.rzodeczko.configuration.domain.valueobject.Money;
import com.rzodeczko.configuration.domain.valueobject.ProductId;

import java.util.Objects;

/**
 * Represents a product entity.
 */
public class Product {
    /** The product ID. */
    private final ProductId id;
    /** The product SKU. */
    private final String sku;
    /** The product name. */
    private final String name;
    /** The unit price. */
    private final Money unitPrice;
    /** Whether the product is active. */
    private boolean active;

    /**
     * Creates a new Product.
     * @param id the product ID
     * @param sku the SKU
     * @param name the name
     * @param unitPrice the unit price
     * @param active whether active
     */
    public Product(ProductId id, String sku, String name, Money unitPrice, boolean active) {
        this.id = Objects.requireNonNull(id, "Product id cannot be null");
        this.sku = Objects.requireNonNull(sku, "Product sku cannot be null");
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Product unitPrice cannot be null");
        this.active = active;
    }

    /**
     * Deactivates the product.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activates the product.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Gets the product ID.
     * @return the ID
     */
    public ProductId getId() {
        return id;
    }

    /**
     * Gets the SKU.
     * @return the SKU
     */
    public String getSku() {
        return sku;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the unit price.
     * @return the unit price
     */
    public Money getUnitPrice() {
        return unitPrice;
    }

    /**
     * Checks if the product is active.
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }
}
