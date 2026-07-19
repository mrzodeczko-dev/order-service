package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.dto.PlaceOrderResultDto;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.order.*;
import com.rzodeczko.application.command.order.*;
import com.rzodeczko.application.port.OrderAtomicPort;
import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.application.port.data.PaymentInitData;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderLifecycleServiceImplTest {

    private OrderLifecycleServiceImpl service;
    private CreateDraftOrderHandler createDraftOrderHandler;
    private PlaceOrderHandler placeOrderHandler;
    private FulfillOrderHandler fulfillOrderHandler;
    private CancelOrderHandler cancelOrderHandler;
    private ReleaseStockHandler releaseStockHandler;
    private CheckStockAvailabilityHandler checkStockAvailabilityHandler;
    private PaymentPort paymentPort;
    private OrderRepository orderRepository;
    private OrderAtomicPort orderAtomicPort;

    private static final Currency PLN = Currency.getInstance("PLN");

    @BeforeEach
    void setUp() {
        createDraftOrderHandler = mock(CreateDraftOrderHandler.class);
        placeOrderHandler = mock(PlaceOrderHandler.class);
        fulfillOrderHandler = mock(FulfillOrderHandler.class);
        cancelOrderHandler = mock(CancelOrderHandler.class);
        releaseStockHandler = mock(ReleaseStockHandler.class);
        checkStockAvailabilityHandler = mock(CheckStockAvailabilityHandler.class);
        paymentPort = mock(PaymentPort.class);
        orderRepository = mock(OrderRepository.class);
        orderAtomicPort = mock(OrderAtomicPort.class);

        service = new OrderLifecycleServiceImpl(
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

    @Nested
    class CreateDraft {

        @Test
        void shouldCreateDraftAndReturnDto() {
            UUID storeId = UUID.randomUUID();
            Order order = new Order(OrderId.newId(), new StoreId(storeId));

            when(createDraftOrderHandler.handle(any(CreateDraftOrderCommand.class)))
                    .thenReturn(order);

            CreateOrderDto result = service.createDraft(storeId);

            assertThat(result.orderId()).isEqualTo(order.getId().id());
            assertThat(result.status()).isEqualTo("DRAFT");
            verify(createDraftOrderHandler).handle(any(CreateDraftOrderCommand.class));
        }
    }

    @Nested
    class PlaceOrder {

        @Test
        void shouldPlaceOrderInitPaymentAndSaveAtomically() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            Order order = new Order(orderId, storeId);
            order.addItem(new OrderItem(ProductId.newId(), 2, new Money(new BigDecimal("100.00"), PLN)));
            order.assignBuyerDetails("buyer@example.com", "John Doe", "1234567890");
            order.place();

            UUID paymentId = UUID.randomUUID();
            String redirectUrl = "https://payment.com/pay";
            PaymentInitData paymentInitData = new PaymentInitData(paymentId, redirectUrl);

            when(placeOrderHandler.handle(any(PlaceOrderCommand.class))).thenReturn(order);
            when(paymentPort.initPayment(eq(orderId.id()), any(), eq("buyer@example.com"), eq("John Doe")))
                    .thenReturn(paymentInitData);

            PlaceOrderResultDto result = service.placeOrder(
                    orderId.id(), "buyer@example.com", "John Doe", "1234567890"
            );

            assertThat(result.paymentId()).isEqualTo(paymentId);
            assertThat(result.paymentRedirectUrl()).isEqualTo(redirectUrl);
            assertThat(result.order().orderId()).isEqualTo(orderId.id());
            verify(orderAtomicPort).savePlacedOrderAtomically(order, paymentInitData);
        }

        @Test
        void shouldNotSaveAtomicallyWhenPaymentFails() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            Order order = new Order(orderId, storeId);
            order.addItem(new OrderItem(ProductId.newId(), 1, new Money(BigDecimal.TEN, PLN)));
            order.assignBuyerDetails("buyer@example.com", "John Doe", "1234567890");
            order.place();

            when(placeOrderHandler.handle(any())).thenReturn(order);
            when(paymentPort.initPayment(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("Payment service unavailable"));

            assertThatThrownBy(() -> service.placeOrder(
                    orderId.id(), "buyer@example.com", "John Doe", "1234567890"
            )).isInstanceOf(RuntimeException.class);

            verify(orderAtomicPort, never()).savePlacedOrderAtomically(any(), any());
        }
    }

    @Nested
    class ConfirmPayment {

        @Test
        void shouldConfirmPaymentAtomically() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            UUID paymentId = UUID.randomUUID();

            Order order = Order.restore(
                    orderId, storeId, OrderStatus.AWAITING_PAYMENT,
                    "buyer@example.com", "John Doe", "1234567890",
                    paymentId, null, "https://payment.com", null
            );

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            service.confirmPayment(orderId.id(), paymentId);

            verify(orderAtomicPort).confirmPaymentAtomically(orderId.id(), paymentId);
        }

        @Test
        void shouldSkipIfAlreadyPaidWithSamePaymentId() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            UUID paymentId = UUID.randomUUID();

            Order order = Order.restore(
                    orderId, storeId, OrderStatus.PAID,
                    "buyer@example.com", "John Doe", "1234567890",
                    paymentId, null, "https://payment.com", null
            );

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            service.confirmPayment(orderId.id(), paymentId);

            verify(orderAtomicPort, never()).confirmPaymentAtomically(any(), any());
        }

        @Test
        void shouldThrowWhenOrderNotFound() {
            UUID orderId = UUID.randomUUID();
            UUID paymentId = UUID.randomUUID();

            when(orderRepository.findById(new OrderId(orderId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.confirmPayment(orderId, paymentId))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    class FulfillOrder {

        @Test
        void shouldFulfillOrderAndReturnDto() {
            UUID orderId = UUID.randomUUID();
            OrderId oid = new OrderId(orderId);
            Order order = Order.restore(
                    oid, StoreId.newId(), OrderStatus.FULFILLED,
                    "b@e.com", "Name", "123",
                    UUID.randomUUID(), null, null, null
            );
            order.addItemForRestore(new OrderItem(ProductId.newId(), 1, new Money(BigDecimal.TEN, PLN)));

            when(fulfillOrderHandler.handle(any(FulfillOrderCommand.class))).thenReturn(order);

            OrderSummaryDto result = service.fulfillOrder(orderId);

            assertThat(result.status()).isEqualTo(OrderStatus.FULFILLED);
        }
    }

    @Nested
    class CancelOrder {

        @Test
        void shouldCancelOrderAndReleaseStock() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            ProductId productId = ProductId.newId();

            Order order = Order.restore(
                    orderId, storeId, OrderStatus.CANCELLED,
                    "b@e.com", "Name", "123",
                    null, null, null, null
            );
            order.addItemForRestore(new OrderItem(productId, 3, new Money(BigDecimal.TEN, PLN)));

            when(cancelOrderHandler.handle(any(CancelOrderCommand.class))).thenReturn(order);

            OrderSummaryDto result = service.cancelOrder(orderId.id());

            assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
            verify(releaseStockHandler).handle(argThat(cmd ->
                    cmd.storeId().equals(storeId.id()) &&
                            cmd.productId().equals(productId.id()) &&
                            cmd.quantity() == 3
            ));
        }

        @Test
        void shouldNotFailWhenStockReleaseThrows() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();

            Order order = Order.restore(
                    orderId, storeId, OrderStatus.CANCELLED,
                    "b@e.com", "Name", "123",
                    null, null, null, null
            );
            order.addItemForRestore(new OrderItem(ProductId.newId(), 1, new Money(BigDecimal.TEN, PLN)));

            when(cancelOrderHandler.handle(any())).thenReturn(order);
            doThrow(new RuntimeException("Stock release failed")).when(releaseStockHandler).handle(any());

            OrderSummaryDto result = service.cancelOrder(orderId.id());

            assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    class MoveOrderToAnotherStore {

        @Test
        void shouldMoveOrderToNewStore() {
            OrderId orderId = OrderId.newId();
            StoreId oldStoreId = StoreId.newId();
            StoreId newStoreId = StoreId.newId();
            ProductId productId = ProductId.newId();

            Order order = new Order(orderId, oldStoreId);
            order.addItem(new OrderItem(productId, 2, new Money(BigDecimal.TEN, PLN)));

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            OrderSummaryDto result = service.moveOrderToAnotherStore(
                    orderId.id(), oldStoreId.id(), newStoreId.id()
            );

            assertThat(result.storeId()).isEqualTo(newStoreId.id());
            verify(orderRepository).save(order);
            verify(checkStockAvailabilityHandler).handle(argThat(cmd ->
                    cmd.storeId().equals(newStoreId.id()) &&
                            cmd.productId().equals(productId.id()) &&
                            cmd.requestedQuantity() == 2
            ));
        }

        @Test
        void shouldThrowWhenOrderNotDraft() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();
            UUID paymentId = UUID.randomUUID();

            Order order = Order.restore(
                    orderId, storeId, OrderStatus.PLACED,
                    "b@e.com", "Name", "123",
                    null, null, null, null
            );

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.moveOrderToAnotherStore(
                    orderId.id(), storeId.id(), UUID.randomUUID()
            )).isInstanceOf(InvalidOrderStateException.class);
        }

        @Test
        void shouldThrowWhenOldStoreDoesNotMatch() {
            OrderId orderId = OrderId.newId();
            StoreId storeId = StoreId.newId();

            Order order = new Order(orderId, storeId);

            when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.moveOrderToAnotherStore(
                    orderId.id(), UUID.randomUUID(), UUID.randomUUID()
            )).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Provided old store does not match current store");
        }
    }
}
