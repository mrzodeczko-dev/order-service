package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.handler.order.AddItemToOrderHandler;
import com.rzodeczko.application.handler.order.RemoveItemFromOrderHandler;
import com.rzodeczko.application.handler.order.ReplaceProductInOrderHandler;
import com.rzodeczko.application.command.order.AddItemToOrderCommand;
import com.rzodeczko.application.command.order.RemoveItemFromOrderCommand;
import com.rzodeczko.application.command.order.ReplaceProductInOrderCommand;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderItemServiceImplTest {

    private OrderItemServiceImpl service;
    private AddItemToOrderHandler addItemHandler;
    private RemoveItemFromOrderHandler removeItemHandler;
    private ReplaceProductInOrderHandler replaceProductHandler;

    private static final Currency PLN = Currency.getInstance("PLN");

    @BeforeEach
    void setUp() {
        addItemHandler = mock(AddItemToOrderHandler.class);
        removeItemHandler = mock(RemoveItemFromOrderHandler.class);
        replaceProductHandler = mock(ReplaceProductInOrderHandler.class);
        service = new OrderItemServiceImpl(addItemHandler, removeItemHandler, replaceProductHandler);
    }

    @Test
    void shouldAddItemAndReturnSummary() {
        OrderId orderId = OrderId.newId();
        ProductId productId = ProductId.newId();
        Order order = new Order(orderId, StoreId.newId());
        order.addItem(new OrderItem(productId, 3, new Money(new BigDecimal("50.00"), PLN)));

        when(addItemHandler.handle(any(AddItemToOrderCommand.class))).thenReturn(order);

        OrderSummaryDto result = service.addItem(orderId.id(), productId.id(), 3, new BigDecimal("50.00"));

        assertThat(result.orderId()).isEqualTo(orderId.id());
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().quantity()).isEqualTo(3);
        verify(addItemHandler).handle(argThat(cmd ->
                cmd.orderId().equals(orderId.id()) &&
                        cmd.productId().equals(productId.id()) &&
                        cmd.quantity() == 3
        ));
    }

    @Test
    void shouldRemoveItemAndReturnSummary() {
        OrderId orderId = OrderId.newId();
        ProductId productId = ProductId.newId();
        Order order = new Order(orderId, StoreId.newId());

        when(removeItemHandler.handle(any(RemoveItemFromOrderCommand.class))).thenReturn(order);

        OrderSummaryDto result = service.removeItem(orderId.id(), productId.id());

        assertThat(result.orderId()).isEqualTo(orderId.id());
        assertThat(result.items()).isEmpty();
        verify(removeItemHandler).handle(argThat(cmd ->
                cmd.orderId().equals(orderId.id()) &&
                        cmd.productId().equals(productId.id())
        ));
    }

    @Test
    void shouldReplaceProductAndReturnSummary() {
        OrderId orderId = OrderId.newId();
        UUID oldProductId = UUID.randomUUID();
        ProductId newProductId = ProductId.newId();
        UUID storeId = UUID.randomUUID();
        Order order = new Order(orderId, new StoreId(storeId));
        order.addItem(new OrderItem(newProductId, 5, new Money(new BigDecimal("25.00"), PLN)));

        when(replaceProductHandler.handle(any(ReplaceProductInOrderCommand.class))).thenReturn(order);

        OrderSummaryDto result = service.replaceProductInOrder(
                orderId.id(), oldProductId, newProductId.id(), 5, new BigDecimal("25.00"), storeId
        );

        assertThat(result.orderId()).isEqualTo(orderId.id());
        assertThat(result.items()).hasSize(1);
        verify(replaceProductHandler).handle(argThat(cmd ->
                cmd.orderId().equals(orderId.id()) &&
                        cmd.oldProductId().equals(oldProductId) &&
                        cmd.newProductId().equals(newProductId.id()) &&
                        cmd.newQuantity() == 5
        ));
    }
}
