package com.rzodeczko.domain.model.store;



import com.rzodeczko.domain.valueobject.StoreId;

import java.util.Objects;

/**
 * Represents a store entity.
 */
public class Store {
    /** The store ID. */
    private final StoreId id;
    /** The store code. */
    private final String code;
    /** The store name. */
    private final String name;
    /** Whether the store is active. */
    private boolean active;

    /**
     * Creates a new Store.
     * @param id the store ID
     * @param code the store code
     * @param name the store name
     * @param active whether active
     */
    public Store(StoreId id, String code, String name, boolean active) {
        this.id = Objects.requireNonNull(id, "Store id cannot be null");
        this.code = Objects.requireNonNull(code, "Store code cannot be null");
        this.name = Objects.requireNonNull(name, "Store name cannot be null");
        this.active = active;
    }

    /**
     * Deactivates the store.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Activates the store.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Gets the store ID.
     * @return the ID
     */
    public StoreId getId() {
        return id;
    }

    /**
     * Gets the store code.
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the store name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the store is active.
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }
}
