package com.rzodeczko.domain.model.product;

import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.ProductId;

import java.math.BigDecimal;
import java.util.Objects;

public class Product {
    private final ProductId id;
    private final String sku;
    private final String name;
    private final Money unitPrice;
    private final BigDecimal taxRate;
    private boolean active;

    public Product(ProductId id, String sku, String name, Money unitPrice, BigDecimal taxRate, boolean active) {
        this.id = Objects.requireNonNull(id, "Product id cannot be null");
        this.sku = Objects.requireNonNull(sku, "Product sku cannot be null");
        this.name = Objects.requireNonNull(name, "Product name cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Product unitPrice cannot be null");
        this.taxRate = Objects.requireNonNull(taxRate, "Product taxRate cannot be null");
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public ProductId getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public boolean isActive() {
        return active;
    }
}
