package com.rzodeczko.domain.valueobject;

import java.util.UUID;

/**
 * Value object representing a store identifier.
 */
public record StoreId(UUID id) {
    /**
     * Validates that the ID is not null.
     */
    public StoreId {
        if (id == null) {
            throw new IllegalArgumentException("Store id cannot be null");
        }
    }

    /**
     * Creates a new random store ID.
     * @return a new StoreId
     */
    public static StoreId newId() {
        return new StoreId(UUID.randomUUID());
    }
}
