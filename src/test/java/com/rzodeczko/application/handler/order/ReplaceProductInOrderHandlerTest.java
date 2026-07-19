package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.application.command.order.ReplaceProductInOrderCommand;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.model.product.Product;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.repository.ProductRepository;
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

class ReplaceProductInOrderHandlerTest {

    private ReplaceProductInOrderHandler handler;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private CheckStockAvailabilityHandler checkStockAvailabilityHandler;

    private UUID orderId;
    private UUID oldProductId;
    private UUID newProductId;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        checkStockAvailabilityHandler = mock(CheckStockAvailabilityHandler.class);
        handler = new ReplaceProductInOrderHandler(orderRepository, productRepository, checkStockAvailabilityHandler);

        orderId = UUID.randomUUID();
        oldProductId = UUID.randomUUID();
        newProductId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Test
    void shouldReplaceProductWithCustomPrice() {
        // given
        Order order = new Order(new OrderId(orderId), new StoreId(storeId));
        order.addItem(new OrderItem(new ProductId(oldProductId), 2, new Money(BigDecimal.TEN, Currency.getInstance("PLN"))));

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        Product newProduct = new Product(
                new ProductId(newProductId), "SKU-NEW", "New Product",
                new Money(BigDecimal.valueOf(50), Currency.getInstance("PLN")),
                BigDecimal.valueOf(0.23), true
        );
        when(productRepository.findById(new ProductId(newProductId))).thenReturn(Optional.of(newProduct));

        var command = new ReplaceProductInOrderCommand(orderId, oldProductId, newProductId, 3, BigDecimal.valueOf(99), storeId);

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getProductId().id()).isEqualTo(newProductId);
        assertThat(result.getItems().getFirst().getQuantity()).isEqualTo(3);
        assertThat(result.getItems().getFirst().getUnitPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(99));
        verify(orderRepository).save(order);
        verify(checkStockAvailabilityHandler).handle(any(CheckStockAvailabilityCommand.class));
    }

    @Test
    void shouldUseProductPriceWhenCustomPriceIsNull() {
        // given
        Order order = new Order(new OrderId(orderId), new StoreId(storeId));
        order.addItem(new OrderItem(new ProductId(oldProductId), 1, new Money(BigDecimal.TEN, Currency.getInstance("PLN"))));

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        Product newProduct = new Product(
                new ProductId(newProductId), "SKU-NEW", "New Product",
                new Money(BigDecimal.valueOf(50), Currency.getInstance("PLN")),
                BigDecimal.valueOf(0.23), true
        );
        when(productRepository.findById(new ProductId(newProductId))).thenReturn(Optional.of(newProduct));

        var command = new ReplaceProductInOrderCommand(orderId, oldProductId, newProductId, 2, null, storeId);

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems().getFirst().getUnitPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        // given
        when(orderRepository.findById(any())).thenReturn(Optional.empty());
        var command = new ReplaceProductInOrderCommand(orderId, oldProductId, newProductId, 1, BigDecimal.TEN, storeId);

        // when / then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowWhenOrderIsNotDraft() {
        // given
        Order order = Order.restore(
                new OrderId(orderId), new StoreId(storeId), OrderStatus.PLACED,
                "a@b.com", "Buyer", "TAX123", null, null, null, null
        );
        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        var command = new ReplaceProductInOrderCommand(orderId, oldProductId, newProductId, 1, BigDecimal.TEN, storeId);

        // when / then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void shouldCheckStockAvailabilityBeforeReplacing() {
        // given
        Order order = new Order(new OrderId(orderId), new StoreId(storeId));
        order.addItem(new OrderItem(new ProductId(oldProductId), 1, new Money(BigDecimal.TEN, Currency.getInstance("PLN"))));

        when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        Product newProduct = new Product(
                new ProductId(newProductId), "SKU", "Prod",
                new Money(BigDecimal.ONE, Currency.getInstance("PLN")),
                BigDecimal.ZERO, true
        );
        when(productRepository.findById(new ProductId(newProductId))).thenReturn(Optional.of(newProduct));

        var command = new ReplaceProductInOrderCommand(orderId, oldProductId, newProductId, 5, BigDecimal.ONE, storeId);

        // when
        handler.handle(command);

        // then
        verify(checkStockAvailabilityHandler).handle(new CheckStockAvailabilityCommand(storeId, newProductId, 5));
    }
}
