package com.rzodeczko.infrastructure.scheduler;

import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.infrastructure.configuration.properties.SchedulerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationJob {
    private final OrderRepository orderRepository;
    private final PaymentPort paymentPort;
    private final SchedulerProperties properties;

    @Scheduled(fixedDelayString = "${scheduler.reconciliation.interval-ms}")
    @SchedulerLock(name = "payment_reconciliation", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    public void reconcile() {
        long ageMinutes = properties.reconciliation().paymentAgeMinutes();
        Instant cutoff = Instant.now().minus(ageMinutes, ChronoUnit.MINUTES);

        var orphanedOrders = orderRepository.findDraftWithPaymentOlderThan(cutoff);
        if (orphanedOrders.isEmpty()) {
            log.info("PaymentReconciliationJob: no orphaned payments found");
            return;
        }

        log.warn("PaymentReconciliationJob: found {} orphaned payment(s)", orphanedOrders.size());
        orphanedOrders.forEach(order -> {
            UUID paymentId = order.getPaymentId();
            try {
                paymentPort.refundPayment(order.getPaymentId());
                order.clearPaymentData();
                orderRepository.save(order);
                log.info(
                        "PaymentReconciliationJob: orphaned payment cancelled. orderId={}, paymentId={}",
                        order.getId().id(),
                        paymentId
                );
            } catch (Exception e) {
                log.error(
                        "PaymentReconciliationJob: failed. orderId={}, paymentId={}, error={}",
                        order.getId().id(),
                        paymentId,
                        e.getMessage()
                );
            }
        });
    }
}
