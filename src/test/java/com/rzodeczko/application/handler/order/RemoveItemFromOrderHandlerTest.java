package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.order.RemoveItemFromOrderCommand;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
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
 * Unit tests for RemoveItemFromOrderHandler.
 */
class RemoveItemFromOrderHandlerTest {

    private RemoveItemFromOrderHandler handler;
    private OrderRepository orderRepository;
    private OrderId orderId;
    private StoreId storeId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        handler = new RemoveItemFromOrderHandler(orderRepository);
        orderId = OrderId.newId();
        storeId = StoreId.newId();
        order = new Order(orderId, storeId);
    }

    @Test
    void shouldRemoveItemFromOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(
                orderId.id(),
                productId.id()
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).isEmpty();
        verify(orderRepository, times(1)).findById(new OrderId(command.orderId()));
        verify(orderRepository, times(1)).save(result);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        ProductId productId = ProductId.newId();
        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(
                orderId.id(),
                productId.id()
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, times(1)).findById(new OrderId(command.orderId()));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRemoveOnlySpecificItem() {
        // given
        ProductId productId1 = ProductId.newId();
        ProductId productId2 = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item1 = new OrderItem(productId1, 2, unitPrice);
        OrderItem item2 = new OrderItem(productId2, 3, unitPrice);
        order.addItem(item1);
        order.addItem(item2);

        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(
                orderId.id(),
                productId1.id()
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(productId2);
    }

    @Test
    void shouldThrowExceptionWhenItemNotInOrder() {
        // given
        ProductId productId = ProductId.newId();
        ProductId productIdToRemove = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(
                orderId.id(),
                productIdToRemove.id()
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnOrderWithRemovedItem() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);

        RemoveItemFromOrderCommand command = new RemoveItemFromOrderCommand(
                orderId.id(),
                productId.id()
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getItems()).isEmpty();
    }
}

