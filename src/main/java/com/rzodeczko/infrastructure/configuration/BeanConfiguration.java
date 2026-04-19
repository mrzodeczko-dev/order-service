package com.rzodeczko.infrastructure.configuration;

import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.inventory.ReplenishStockHandler;
import com.rzodeczko.application.handler.inventory.ReserveStockHandler;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
/**
 * Configuration class for enabling properties.
 */
public class BeanConfiguration {

    @Bean
    public CheckStockAvailabilityHandler checkStockAvailabilityHandler(
            InventoryRepository inventoryRepository,
            OrderRepository orderRepository
    ) {
        return new CheckStockAvailabilityHandler(inventoryRepository, orderRepository);
    }

    @Bean
    public ReserveStockHandler reserveStockHandler(InventoryRepository inventoryRepository) {
        return new ReserveStockHandler(inventoryRepository);
    }

    @Bean
    public ReleaseStockHandler releaseStockHandler(InventoryRepository inventoryRepository) {
        return new ReleaseStockHandler(inventoryRepository);
    }

    @Bean
    public ReplenishStockHandler replenishStockHandler(InventoryRepository inventoryRepository) {
        return new ReplenishStockHandler(inventoryRepository);
    }
}
