package com.rzodeczko.infrastructure.configuration;

import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
/**
 * Configuration class for enabling properties.
 */
public class BeanConfiguration {
}
