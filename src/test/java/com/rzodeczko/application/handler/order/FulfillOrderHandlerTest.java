package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.FulfillOrderCommand;
import com.rzodeczko.domain.exception.InventoryNotFoundException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.inventory.Inventory;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.repository.InventoryRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FulfillOrderHandler.
 */
class FulfillOrderHandlerTest {

    private FulfillOrderHandler handler;
    private OrderRepository orderRepository;
    private InventoryRepository inventoryRepository;
    private OrderId orderId;
    private StoreId storeId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        handler = new FulfillOrderHandler(orderRepository, inventoryRepository);
        orderId = OrderId.newId();
        storeId = StoreId.newId();
        order = new Order(orderId, storeId);
    }

    @Test
    void shouldFulfillOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();
        order.markAwaitingPayment(paymentId, "http://payment.example.com");
        order.markPaid(paymentId);

        Inventory inventory = new Inventory(storeId, productId, 10, 2);

        FulfillOrderCommand command = new FulfillOrderCommand(orderId.id());

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByStoreAndProduct(storeId, productId))
                .thenReturn(Optional.of(inventory));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.FULFILLED);
        verify(orderRepository, times(1)).save(result);
        verify(inventoryRepository, times(1)).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        FulfillOrderCommand command = new FulfillOrderCommand(orderId.id());

        when(orderRepository.findById(any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void shouldThrowExceptionWhenInventoryNotFound() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();
        order.markAwaitingPayment(paymentId, "http://payment.example.com");
        order.markPaid(paymentId);

        FulfillOrderCommand command = new FulfillOrderCommand(orderId.id());

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByStoreAndProduct(any(), any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InventoryNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldUpdateInventoryForAllItems() {
        // given
        ProductId productId1 = ProductId.newId();
        ProductId productId2 = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item1 = new OrderItem(productId1, 2, unitPrice);
        OrderItem item2 = new OrderItem(productId2, 3, unitPrice);
        order.addItem(item1);
        order.addItem(item2);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();
        order.markAwaitingPayment(paymentId, "http://payment.example.com");
        order.markPaid(paymentId);

        Inventory inventory1 = new Inventory(storeId, productId1, 10, 2);
        Inventory inventory2 = new Inventory(storeId, productId2, 20, 3);

        FulfillOrderCommand command = new FulfillOrderCommand(orderId.id());

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByStoreAndProduct(storeId, productId1))
                .thenReturn(Optional.of(inventory1));
        when(inventoryRepository.findByStoreAndProduct(storeId, productId2))
                .thenReturn(Optional.of(inventory2));

        // when
        handler.handle(command);

        // then
        verify(inventoryRepository, times(1)).saveAll(any());
    }

    @Test
    void shouldReturnFulfilledOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();
        UUID paymentId = UUID.randomUUID();
        order.markAwaitingPayment(paymentId, "http://payment.example.com");
        order.markPaid(paymentId);

        Inventory inventory = new Inventory(storeId, productId, 10, 2);

        FulfillOrderCommand command = new FulfillOrderCommand(orderId.id());

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(inventoryRepository.findByStoreAndProduct(any(), any()))
                .thenReturn(Optional.of(inventory));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.FULFILLED);
    }
}

