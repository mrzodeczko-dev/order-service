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
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.application.port.data.PaymentInitData;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderLifecycleServiceImpl implements OrderLifecycleService {

    private static final Logger log = Logger.getLogger(OrderLifecycleServiceImpl.class.getName());

    private final CreateDraftOrderHandler createDraftOrderHandler;
    private final PlaceOrderHandler placeOrderHandler;
    private final FulfillOrderHandler fulfillOrderHandler;
    private final CancelOrderHandler cancelOrderHandler;
    private final ReleaseStockHandler releaseStockHandler;
    private final CheckStockAvailabilityHandler checkStockAvailabilityHandler;
    private final PaymentPort paymentPort;
    private final OrderRepository orderRepository;
    private final OrderAtomicPort orderAtomicPort;

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
     *
     * HTTP PRZED ATOMOWYM ZAPISEM
     * HTTP sie nie powiedzie -> DRAFT, nic do cofania
     * HTTP OK + DB crash -> osiercona platnosc -> reconciliation job po 30 minutach

     * Co to jest  reconciliation job?
     * Reconciliation Job to scheduler który co godzinę przegląda bazę i pyta: "czy są jakieś zamówienia
     * w statusie DRAFT, które mają paymentId i siedzą tak od ponad 30 minut?". Jeśli tak — to znaczy że
     * coś poszło nie tak, bo normalny flow przez 30 minut już dawno by te zamówienia popchnął do AWAITING_PAYMENT.
     * Wtedy job wywołuje paymentPort.refundPayment() żeby anulować płatność w systemie zewnętrznym i czyści
     * paymentId z zamówienia.
     */
    @Override
    public PlaceOrderResultDto placeOrder(
            UUID orderId,
            String buyerEmail,
            String buyerName,
            String buyerTaxId) {

        // Krok 1: mutacja agregatu w pamieci - tylko odczyt DB
        Order order = placeOrderHandler.handle(new PlaceOrderCommand(orderId, buyerEmail, buyerName, buyerTaxId));

        // Krok 2: HTTP - POZA transakcja DB (DB nadal widzi DRAFT)
        PaymentInitData paymentInitData = paymentPort.initPayment(
                orderId, order.getTotalAmount().amount(), buyerEmail, buyerName
        );

        // Krok 3: Atomowy zapis przez OrderAtomicPort
        // Jedna transakcja: rezerwacja stocku + AWAITING_PAYMENT + paymentId
        orderAtomicPort.savePlacedOrderAtomically(order, paymentInitData);

        log.info("Order placed. orderId=%s, paymentId=%s".formatted(orderId, paymentInitData.paymentId()));

        return new PlaceOrderResultDto(
                OrderSummaryDto.from(order),
                paymentInitData.paymentId(),
                paymentInitData.redirectUrl());
    }

    /**
     * Callback z payment-service
     * Idempotencja - systemy platnosci wysylaja at-least-once
     */
    @Override
    public void confirmPayment(UUID orderId, UUID paymentId) {
        Order order = orderRepository
                .findById(new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        // Duplicat callbacku - cichy return
        if (order.isAlreadyPaidWith(paymentId)) {
            log.info("Order %s already PAID with paymentId=%s, skipping".formatted(orderId, paymentId));
            return;
        }

        // Jedna transakcja: markPaid + INSERT invoice_outbox_tasks
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
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.isDraft()) {
            throw new IllegalStateException("Only DRAFT orders can be moved");
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
