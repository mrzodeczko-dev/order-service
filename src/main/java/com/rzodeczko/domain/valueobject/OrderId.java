package com.rzodeczko.domain.valueobject;

import java.util.UUID;

/**
 * Value object representing an order identifier.
 */
public record OrderId(UUID id) {
    /**
     * Validates that the ID is not null.
     */
    public OrderId {
        if (id == null) {
            throw new IllegalArgumentException("Order id cannot be null");
        }
    }

    /**
     * Creates a new random order ID.
     * @return a new OrderId
     */
    public static OrderId newId() {
        return new OrderId(UUID.randomUUID());
    }
}
