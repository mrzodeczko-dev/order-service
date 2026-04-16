package com.rzodeczko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Main application class for Order Service.
 */
public class OrderServiceApplication {

    /**
     * Starts the Spring Boot application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

}
