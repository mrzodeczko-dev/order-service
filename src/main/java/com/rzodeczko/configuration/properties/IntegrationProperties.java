package com.rzodeczko.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration")
public record IntegrationProperties(Payment payment, Invoice invoice) {
    public record Payment(String url) {}
    public record Invoice(String url) {}
}
