package com.rzodeczko.application.service.order.impl;

import com.rzodeczko.application.command.inventory.CheckStockAvailabilityCommand;
import com.rzodeczko.application.command.inventory.ReleaseStockCommand;
import com.rzodeczko.application.command.order.CancelOrderCommand;
import com.rzodeczko.application.command.order.CreateDraftOrderCommand;
import com.rzodeczko.application.command.order.FulfillOrderCommand;
import com.rzodeczko.application.command.order.PlaceOrderCommand;
import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.dto.PlaceOrderResultDto;
import com.rzodeczko.application.handler.inventory.CheckStockAvailabilityHandler;
import com.rzodeczko.application.handler.inventory.ReleaseStockHandler;
import com.rzodeczko.application.handler.order.CancelOrderHandler;
import com.rzodeczko.application.handler.order.CreateDraftOrderHandler;
import com.rzodeczko.application.handler.order.FulfillOrderHandler;
import com.rzodeczko.application.handler.order.PlaceOrderHandler;
import com.rzodeczko.application.port.OrderAtomicPort;
import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.domain.exception.InvalidOrderStateException;
import com.rzodeczko.domain.exception.OrderNotFoundException;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.application.port.data.PaymentInitData;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of OrderLifecycleService.
 * Manages the complete lifecycle of orders including creation, placement, payment confirmation, and fulfillment.
 */
public class OrderLifecycleServiceImpl implements OrderLifecycleService {

    private static final Logger log = Logger.getLogger(OrderLifecycleServiceImpl.class.getName());

    /** The create draft order handler. */
    private final CreateDraftOrderHandler createDraftOrderHandler;
    /** The place order handler. */
    private final PlaceOrderHandler placeOrderHandler;
    /** The fulfill order handler. */
    private final FulfillOrderHandler fulfillOrderHandler;
    /** The cancel order handler. */
    private final CancelOrderHandler cancelOrderHandler;
    /** The release stock handler. */
    private final ReleaseStockHandler releaseStockHandler;
    /** The check stock availability handler. */
    private final CheckStockAvailabilityHandler checkStockAvailabilityHandler;
    /** The payment port. */
    private final PaymentPort paymentPort;
    /** The order repository. */
    private final OrderRepository orderRepository;
    /** The order atomic port. */
    private final OrderAtomicPort orderAtomicPort;

    /**
     * Creates a new OrderLifecycleServiceImpl.
     * @param createDraftOrderHandler the create draft order handler
     * @param placeOrderHandler the place order handler
     * @param fulfillOrderHandler the fulfill order handler
     * @param cancelOrderHandler the cancel order handler
     * @param releaseStockHandler the release stock handler
     * @param checkStockAvailabilityHandler the check stock availability handler
     * @param paymentPort the payment port
     * @param orderRepository the order repository
     * @param orderAtomicPort the order atomic port
     */
    public OrderLifecycleServiceImpl(
            CreateDraftOrderHandler createDraftOrderHandler,
            PlaceOrderHandler placeOrderHandler,
            FulfillOrderHandler fulfillOrderHandler,
            CancelOrderHandler cancelOrderHandler,
            ReleaseStockHandler releaseStockHandler,
            CheckStockAvailabilityHandler checkStockAvailabilityHandler,
            PaymentPort paymentPort,
            OrderRepository orderRepository,
            OrderAtomicPort orderAtomicPort) {
        this.createDraftOrderHandler = createDraftOrderHandler;
        this.placeOrderHandler = placeOrderHandler;
        this.fulfillOrderHandler = fulfillOrderHandler;
        this.cancelOrderHandler = cancelOrderHandler;
        this.releaseStockHandler = releaseStockHandler;
        this.checkStockAvailabilityHandler = checkStockAvailabilityHandler;
        this.paymentPort = paymentPort;
        this.orderRepository = orderRepository;
        this.orderAtomicPort = orderAtomicPort;
    }

    @Override
    public CreateOrderDto createDraft(UUID storeId) {
        return CreateOrderDto.from(createDraftOrderHandler.handle(
                new CreateDraftOrderCommand(storeId)
        ));
    }

    /**
     * Places an order with payment initialization.
     *
     * Process:
     * Step 1: Mutate aggregate in memory - only read from DB
     * Step 2: HTTP call to payment service - OUTSIDE DB transaction (DB still sees DRAFT)
     * Step 3: Atomic save via OrderAtomicPort
     *         Single transaction: stock reservation + AWAITING_PAYMENT + paymentId
     *
     * Resilience: If HTTP succeeds but DB crashes → orphaned payment → reconciliation job after 30 minutes
     * If HTTP fails → DRAFT remains, nothing to clean up
     *
     * Reconciliation Job: Hourly scheduler checks for DRAFT orders with paymentId older than 30 minutes,
     * calls paymentPort.refundPayment() to cancel payment in external system and cleans up paymentId.
     *
     * @param orderId the order ID
     * @param buyerEmail the buyer email
     * @param buyerName the buyer name
     * @param buyerTaxId the buyer tax ID
     * @return the place order result DTO
     */
    @Override
    public PlaceOrderResultDto placeOrder(
            UUID orderId,
            String buyerEmail,
            String buyerName,
            String buyerTaxId) {

        Order order = placeOrderHandler.handle(new PlaceOrderCommand(orderId, buyerEmail, buyerName, buyerTaxId));

        PaymentInitData paymentInitData = paymentPort.initPayment(
                orderId, order.getTotalAmount().amount(), buyerEmail, buyerName
        );

        orderAtomicPort.savePlacedOrderAtomically(order, paymentInitData);

        log.info("Order placed. orderId=%s, paymentId=%s".formatted(orderId, paymentInitData.paymentId()));

        return new PlaceOrderResultDto(
                OrderSummaryDto.from(order),
                paymentInitData.paymentId(),
                paymentInitData.redirectUrl());
    }

    /**
     * Confirms payment for an order.
     *
     * Callback from payment service with idempotency guarantee.
     * Payment systems send at-least-once delivery, so duplicate callbacks may occur.
     * If order is already PAID with the same paymentId, silently ignore (idempotent).
     *
     * Single atomic operation: mark order as PAID and create invoice_outbox_tasks.
     *
     * @param orderId the order ID
     * @param paymentId the payment ID
     */
    @Override
    public void confirmPayment(UUID orderId, UUID paymentId) {
        Order order = orderRepository
                .findById(new OrderId(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.isAlreadyPaidWith(paymentId)) {
            log.info("Order %s already PAID with paymentId=%s, skipping".formatted(orderId, paymentId));
            return;
        }

        orderAtomicPort.confirmPaymentAtomically(orderId, paymentId);

        log.info("Order %s marked as PAID, outbox task created. paymentId=%s".formatted(orderId, paymentId));
    }

    @Override
    public OrderSummaryDto fulfillOrder(UUID orderId) {
        return OrderSummaryDto.from(fulfillOrderHandler.handle(new FulfillOrderCommand(orderId)));
    }

    @Override
    public OrderSummaryDto cancelOrder(UUID orderId) {
        Order order = cancelOrderHandler.handle(new CancelOrderCommand(orderId));

        order
                .getItems()
                .forEach(item -> {
                    try {
                        releaseStockHandler.handle(new ReleaseStockCommand(
                                order.getStoreId().id(),
                                item.getProductId().id(),
                                item.getQuantity()
                        ));
                    } catch (Exception e) {
                        log.warning("Stock release failed for product=%s during cancel: %s".formatted(
                                item.getProductId().id(),
                                e.getMessage()
                        ));
                    }
                });

        return OrderSummaryDto.from(order);
    }

    @Override
    public OrderSummaryDto moveOrderToAnotherStore(UUID orderId, UUID oldStoreId, UUID newStoreId) {
        Order order = orderRepository
                .findById(new OrderId(orderId))
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!order.isDraft()) {
            throw new InvalidOrderStateException("Only DRAFT orders can be moved");
        }

        if (!order.getStoreId().id().equals(oldStoreId)) {
            throw new IllegalArgumentException("Provided old store does not match current store");
        }

        order
                .getItems()
                .stream()
                .collect(Collectors.groupingBy(
                        item -> item.getProductId().id(),
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .forEach((productId, qty) -> {
                    checkStockAvailabilityHandler.handle(new CheckStockAvailabilityCommand(
                            newStoreId, productId, qty
                    ));
                });

        order.relocate(new StoreId(newStoreId));
        orderRepository.save(order);
        return OrderSummaryDto.from(order);
    }
}
