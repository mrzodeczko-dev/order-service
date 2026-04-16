package com.rzodeczko.configuration.domain.model.order;

import com.rzodeczko.configuration.domain.valueobject.Money;
import com.rzodeczko.configuration.domain.valueobject.ProductId;

import java.util.Objects;

/**
 * Represents an item in an order.
 */
public class OrderItem {
    /** The product ID. */
    private final ProductId productId;
    /** The quantity. */
    private int quantity;
    /** The unit price. */
    private final Money unitPrice;

    /**
     * Creates a new OrderItem.
     * @param productId the product ID
     * @param quantity the quantity
     * @param unitPrice the unit price
     * @throws IllegalArgumentException if quantity <= 0
     */
    public OrderItem(ProductId productId, int quantity, Money unitPrice) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.productId = Objects.requireNonNull(productId, "ProductId cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice cannot be null");
        this.quantity = quantity;
    }

    /**
     * Calculates the line total.
     * @return the total for this item
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    /**
     * Checks if same product and price.
     * @param otherProductId the other product ID
     * @param otherUnitPrice the other unit price
     * @return true if same
     */
    public boolean isSameProductAndPrice(ProductId otherProductId, Money otherUnitPrice) {
        return productId.equals(otherProductId) && unitPrice.equals(otherUnitPrice);
    }

    /**
     * Changes the quantity.
     * @param newQuantity the new quantity
     * @throws IllegalArgumentException if newQuantity <= 0
     */
    public void changeQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.quantity = newQuantity;
    }

    /**
     * Gets the product ID.
     * @return the product ID
     */
    public ProductId getProductId() {
        return productId;
    }

    /**
     * Gets the quantity.
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the unit price.
     * @return the unit price
     */
    public Money getUnitPrice() {
        return unitPrice;
    }
}
