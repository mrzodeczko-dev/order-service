package com.rzodeczko.infrastructure.outbox;

import com.rzodeczko.application.port.InvoicePort;
import com.rzodeczko.application.port.NotificationPort;
import com.rzodeczko.application.port.ProductNameResolver;
import com.rzodeczko.application.port.data.InvoiceItemData;
import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.outbox.InvoiceOutboxTask;
import com.rzodeczko.domain.repository.InvoiceOutboxTaskRepository;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.domain.valueobject.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceOutboxSender {
    private final InvoicePort invoicePort;
    private final NotificationPort notificationPort;
    private final OrderRepository orderRepository;
    private final InvoiceOutboxTaskRepository invoiceOutboxTaskRepository;
    private final ProductNameResolver productNameResolver;

    /**
     * REQUIRES_NEW - kazdy task ma wlasna transakcje
     * Blad w tasku nr 7 nie rollbackuje taska nr 3
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(InvoiceOutboxTask task) {
        Order order = orderRepository
                .findById(new OrderId(task.getOrderId()))
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for outbox task. orderId=" + task.getOrderId()
                ));

        // Idempotencja - faktur moze byc juz przypisana (crash po HTTP, przed markSent)
        if (order.getInvoiceId() != null) {
            log.info("Invoice already assigned for order={}, marking task as sent", task.getOrderId());
            task.markSent();
            invoiceOutboxTaskRepository.save(task);
            return;
        }

        try {
            List<InvoiceItemData> invoiceItems = order
                    .getItems()
                    .stream()
                    .map(item -> new InvoiceItemData(
                            productNameResolver.resolve(item.getProductId()),
                            item.getQuantity(),
                            item.getUnitPrice().amount()
                    )).toList();

            // HTTP do invoice-service - REQUIRES_NEW = oddzielna transakcja
            UUID invoiceId = invoicePort.generateInvoice(
                    task.getOrderId(),
                    order.getBuyerTaxId(),
                    order.getBuyerName(),
                    invoiceItems
            );

            // Atomowy zapis w tej samej REQUIRES_NEW transakcji
            order.assignInvoice(invoiceId);
            orderRepository.save(order);
            task.markSent();
            invoiceOutboxTaskRepository.save(task);

            log.info("Invoice outbox task sent. orderId={}, invoiceId={}", task.getOrderId(), invoiceId);

            try {
                notificationPort.sendOrderConfirmation(task.getOrderId(), order.getPaymentId(), invoiceId);
            } catch (Exception e) {
                log.error(
                        "Notification failed for order={}. Invoice assigned. Error={}",
                        task.getOrderId(),
                        e.getMessage()
                );
            }
        } catch (Exception e) {
            task.markFailed();
            invoiceOutboxTaskRepository.save(task);

            if (task.isExhausted()) {
                log.error(
                        "ALERT: Invoice outbox task EXHAUSTED, orderId={}. Manual intervention required. retryCount={}",
                        task.getOrderId(),
                        task.getRetryCount());
            } else {
                log.warn(
                        "Invoice outbox task failed, will retry. orderId={}, retryCount={}, reason={}",
                        task.getOrderId(),
                        task.getRetryCount(),
                        e.getMessage()
                );
            }
        }
    }
}
