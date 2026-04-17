package com.rzodeczko.infrastructure.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration")
/**
 * Configuration properties for external integrations.
 */
public record IntegrationProperties(Payment payment, Invoice invoice) {
    /**
     * Payment service configuration.
     */
    public record Payment(String url) {}
    /**
     * Invoice service configuration.
     */
    public record Invoice(String url) {}
}
