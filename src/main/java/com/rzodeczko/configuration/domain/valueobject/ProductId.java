package com.rzodeczko.configuration.domain.valueobject;

import java.util.UUID;

/**
 * Value object representing a product identifier.
 */
public record ProductId(UUID id) {
    /**
     * Validates that the ID is not null.
     */
    public ProductId {
        if (id == null) {
            throw new IllegalArgumentException("Product id cannot be null");
        }
    }

    /**
     * Creates a new random product ID.
     * @return a new ProductId
     */
    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }
}
