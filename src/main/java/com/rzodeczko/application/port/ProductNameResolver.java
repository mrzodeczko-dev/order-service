package com.rzodeczko.application.port;


import com.rzodeczko.domain.valueobject.ProductId;

/**
 * Port for resolving product names by ID.
 * Separated as a port because in the future products may be in a separate microservice - then it becomes an HTTP call.
 */
public interface ProductNameResolver {
    /**
     * Resolves the product name for the given product ID.
     * @param productId the product ID
     * @return the product name
     */
    String resolve(ProductId productId);
}
