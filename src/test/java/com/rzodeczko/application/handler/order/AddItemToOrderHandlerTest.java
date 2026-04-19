package com.rzodeczko.application.handler.order;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.application.command.order.AddItemToOrderCommand;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.exception.ProductNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddItemToOrderHandler.
 */
class AddItemToOrderHandlerTest {

    private AddItemToOrderHandler handler;
    private OrderRepository orderRepository;
    private ProductRepository productRepository;
    private CheckStockAvailabilityHandler checkStockAvailabilityHandler;
    private OrderId orderId;
    private StoreId storeId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductRepository.class);
        checkStockAvailabilityHandler = mock(CheckStockAvailabilityHandler.class);
        handler = new AddItemToOrderHandler(orderRepository, productRepository, checkStockAvailabilityHandler);
        orderId = OrderId.newId();
        storeId = StoreId.newId();
        order = new Order(orderId, storeId);
    }

    @Test
    void shouldAddItemToOrder() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "SKU123", "Product", unitPrice, true);

        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(new OrderId(command.orderId())))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(new ProductId(command.productId())))
                .thenReturn(Optional.of(product));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getProductId()).isEqualTo(productId);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(orderRepository, times(1)).save(result);
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        // given
        ProductId productId = ProductId.newId();
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        // given
        ProductId productId = ProductId.newId();
        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(any()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNotInDraftState() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        OrderItem item = new OrderItem(productId, 2, unitPrice);
        order.addItem(item);
        order.assignBuyerDetails("buyer@example.com", "John Doe", "123456789");
        order.place();

        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessage("Can modify order only in DRAFT state");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldMergeItemsWithSameProductAndPrice() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "SKU123", "Product", unitPrice, true);
        OrderItem existingItem = new OrderItem(productId, 2, unitPrice);
        order.addItem(existingItem);

        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                3,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(any()))
                .thenReturn(Optional.of(product));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldUseProductPriceWhenUnitPriceNotProvided() {
        // given
        ProductId productId = ProductId.newId();
        Money productPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "SKU123", "Product", productPrice, true);

        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(any()))
                .thenReturn(Optional.of(product));

        // when
        Order result = handler.handle(command);

        // then
        assertThat(result.getItems().get(0).getUnitPrice()).isEqualTo(productPrice);
    }

    @Test
    void shouldCheckStockAvailability() {
        // given
        ProductId productId = ProductId.newId();
        Money unitPrice = new Money(new BigDecimal("10.50"), Currency.getInstance("PLN"));
        Product product = new Product(productId, "SKU123", "Product", unitPrice, true);

        AddItemToOrderCommand command = new AddItemToOrderCommand(
                orderId.id(),
                productId.id(),
                2,
                null
        );

        when(orderRepository.findById(any()))
                .thenReturn(Optional.of(order));
        when(productRepository.findById(any()))
                .thenReturn(Optional.of(product));

        // when
        handler.handle(command);

        // then
        verify(checkStockAvailabilityHandler, times(1)).handle(any(CheckStockAvailabilityCommand.class));
    }
}

