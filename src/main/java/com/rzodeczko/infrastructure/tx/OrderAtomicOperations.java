package com.rzodeczko.infrastructure.tx;


import com.rzodeczko.application.command.inventory.ReserveStockCommand;
import com.rzodeczko.application.handler.inventory.ReserveStockHandler;
import com.rzodeczko.application.port.OrderAtomicPort;
import com.rzodeczko.application.port.data.PaymentInitData;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Jedyne miejsce @Transactional dla lifecycle atomowych operacji.
 * OrderLifecycleServiceImpl wstrzykuje ten bean jak kazdy inny port czyli masz powtarzalna konfiguracje
 * dopasowana do struktury Twojego projektu.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderAtomicOperations implements OrderAtomicPort {

    private final OrderRepository orderRepository;
    private final InvoiceOutboxTaskRepository invoiceOutboxTaskRepository;
    private final ReserveStockHandler reserveStockHandler;

    /*
        Jedna transakcja:
        1. Rezerwacja stocku dla kazdego itemu.
        2. order.markAwaitingPayment
        3. orderRepository.save
     */
    @Override
    @Transactional
    public void savePlacedOrderAtomically(Order order, PaymentInitData paymentInitData) {
        order
                .getItems()
                .forEach(item ->
                        reserveStockHandler.handle(new ReserveStockCommand(
                                order.getStoreId().id(),
                                item.getProductId().id(),
                                item.getQuantity()
                        )));
        order.markAwaitingPayment(paymentInitData.paymentId(), paymentInitData.redirectUrl());
        orderRepository.save(order);
    }

    /**
     * Jedna transakcja
     * 1. order.markPaid
     * 2. orderRepository.save
     * 3. INSERT invoice_outbox_tasks
     * Crash miedzy 2 a 3 niemozliwy - jedna transakcja
     */
    @Override
    @Transactional
    public void confirmPaymentAtomically(UUID orderId, UUID paymentId) {
        Order order = orderRepository
                .findById(new OrderId(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.markPaid(paymentId);
        orderRepository.save(order);

        InvoiceOutboxTask task = InvoiceOutboxTask.create(orderId);
        invoiceOutboxTaskRepository.save(task);
    }
}
