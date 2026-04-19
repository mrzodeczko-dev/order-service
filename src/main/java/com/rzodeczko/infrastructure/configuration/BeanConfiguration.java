package com.rzodeczko.infrastructure.configuration;

import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.inventory.ReplenishStockHandler;
import com.rzodeczko.application.handler.inventory.ReserveStockHandler;
import com.rzodeczko.application.handler.order.*;
import com.rzodeczko.application.port.OrderAtomicPort;
import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.application.service.inventory.InventoryService;
import com.rzodeczko.application.service.inventory.impl.InventoryServiceImpl;
import com.rzodeczko.application.service.order.OrderItemService;
import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.application.service.order.OrderQueryService;
import com.rzodeczko.application.service.order.impl.OrderItemServiceImpl;
import com.rzodeczko.application.service.order.impl.OrderLifecycleServiceImpl;
import com.rzodeczko.application.service.order.impl.OrderQueryServiceImpl;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.repository.ProductRepository;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import com.rzodeczko.infrastructure.configuration.properties.SchedulerProperties;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties({IntegrationProperties.class, SchedulerProperties.class})
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class BeanConfiguration {

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        HttpClient httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofMillis(2000))
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(5000));
        return builder -> builder.requestFactory(requestFactory);
    }

    @Bean
    public LockProvider lockProvider(JdbcTemplate jdbcTemplate) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration
                .builder()
                .withJdbcTemplate(jdbcTemplate)
                .usingDbTime()
                .build());
    }

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

    @Bean
    public CreateDraftOrderHandler createDraftOrderHandler(OrderRepository orderRepository) {
        return new CreateDraftOrderHandler(orderRepository);
    }

    @Bean
    public PlaceOrderHandler placeOrderHandler(OrderRepository orderRepository) {
        return new PlaceOrderHandler(orderRepository);
    }

    @Bean
    public FulfillOrderHandler fulfillOrderHandler(
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository
    ) {
        return new FulfillOrderHandler(orderRepository, inventoryRepository);
    }

    @Bean
    public CancelOrderHandler cancelOrderHandler(OrderRepository orderRepository) {
        return new CancelOrderHandler(orderRepository);
    }

    @Bean
    public RemoveItemFromOrderHandler removeItemFromOrderHandler(OrderRepository orderRepository) {
        return new RemoveItemFromOrderHandler(orderRepository);
    }

    @Bean
    public AddItemToOrderHandler addItemToOrderHandler(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CheckStockAvailabilityHandler checkStockAvailabilityHandler
    ) {
        return new AddItemToOrderHandler(orderRepository, productRepository, checkStockAvailabilityHandler);
    }

    @Bean
    public ReplaceProductInOrderHandler replaceProductInOrderHandler(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            CheckStockAvailabilityHandler checkStockAvailabilityHandler
    ) {
        return new ReplaceProductInOrderHandler(orderRepository, productRepository, checkStockAvailabilityHandler);
    }

    @Bean
    public InventoryService inventoryService(
            ReserveStockHandler reserveStockHandler,
            ReleaseStockHandler releaseStockHandler,
            ReplenishStockHandler replenishStockHandler
    ) {
        return new InventoryServiceImpl(reserveStockHandler, releaseStockHandler, replenishStockHandler);
    }

    @Bean("orderLifecycleServiceImpl")
    public OrderLifecycleService orderLifecycleService(
            CreateDraftOrderHandler createDraftOrderHandler,
            PlaceOrderHandler placeOrderHandler,
            FulfillOrderHandler fulfillOrderHandler,
            CancelOrderHandler cancelOrderHandler,
            ReleaseStockHandler releaseStockHandler,
            CheckStockAvailabilityHandler checkStockAvailabilityHandler,
            PaymentPort paymentPort,
            OrderRepository orderRepository,
            OrderAtomicPort orderAtomicPort
    ) {
        return new OrderLifecycleServiceImpl(
                createDraftOrderHandler,
                placeOrderHandler,
                fulfillOrderHandler,
                cancelOrderHandler,
                releaseStockHandler,
                checkStockAvailabilityHandler,
                paymentPort,
                orderRepository,
                orderAtomicPort
        );
    }

    @Bean
    public OrderItemService orderItemService(
            AddItemToOrderHandler addItemToOrderHandler,
            RemoveItemFromOrderHandler removeItemFromOrderHandler,
            ReplaceProductInOrderHandler replaceProductInOrderHandler
    ) {
        return new OrderItemServiceImpl(
                addItemToOrderHandler,
                removeItemFromOrderHandler,
                replaceProductInOrderHandler
        );
    }

    @Bean
    public OrderQueryService orderQueryService(OrderRepository orderRepository) {
        return new OrderQueryServiceImpl(orderRepository);
    }
}
