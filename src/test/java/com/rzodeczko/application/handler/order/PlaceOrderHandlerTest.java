package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.PlaceOrderCommand;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaceOrderHandler.
 */
class PlaceOrderHandlerTest {

    private PlaceOrderHandler handler;
    private OrderRepository orderRepository;
    private OrderId orderId;
    private StoreId storeId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        handler = new PlaceOrderHandler(orderRepository);
        orderId = OrderId.newId();
        storeId = StoreId.newId();
        order = new Order(orderId, storeId);
    }

    @Test
    void shouldPlaceOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        PlaceOrderCommand command = new PlaceOrderCommand(
                orderId.id(),
                "buyer@example.com",
                "John Doe",
                "123456789"
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
        assertThat(result.getBuyerEmail()).isEqualTo("buyer@example.com");
        assertThat(result.getBuyerName()).isEqualTo("John Doe");
        assertThat(result.getBuyerTaxId()).isEqualTo("123456789");
        verify(orderRepository, times(1)).findById(new OrderId(command.orderId()));
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        PlaceOrderCommand command = new PlaceOrderCommand(
                orderId.id(),
                "buyer@example.com",
                "John Doe",
                "123456789"
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found");

        verify(orderRepository, times(1)).findById(new OrderId(command.orderId()));
    }

    @Test
    void shouldAssignBuyerDetailsBeforePlacing() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        PlaceOrderCommand command = new PlaceOrderCommand(
                orderId.id(),
                "buyer@example.com",
                "John Doe",
                "123456789"
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        handler.handle(command);

        // then
        assertThat(order.getBuyerEmail()).isEqualTo("buyer@example.com");
        assertThat(order.getBuyerName()).isEqualTo("John Doe");
        assertThat(order.getBuyerTaxId()).isEqualTo("123456789");
    }

    @Test
    void shouldThrowExceptionWhenOrderHasNoItems() {
        // given
        PlaceOrderCommand command = new PlaceOrderCommand(
                orderId.id(),
                "buyer@example.com",
                "John Doe",
                "123456789"
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage("Order cannot be placed without items");
    }

    @Test
    void shouldReturnPlacedOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        PlaceOrderCommand command = new PlaceOrderCommand(
                orderId.id(),
                "buyer@example.com",
                "John Doe",
                "123456789"
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PLACED);
    }
}
