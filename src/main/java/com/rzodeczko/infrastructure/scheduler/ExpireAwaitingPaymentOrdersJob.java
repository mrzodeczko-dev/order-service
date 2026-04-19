package com.rzodeczko.infrastructure.scheduler;

import com.rzodeczko.application.service.order.OrderLifecycleService;
import com.rzodeczko.domain.repository.OrderRepository;
import com.rzodeczko.infrastructure.configuration.properties.SchedulerProperties;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@Slf4j
public class ExpireAwaitingPaymentOrdersJob {
    private final OrderRepository orderRepository;
    private final OrderLifecycleService orderLifecycleService;
    private final SchedulerProperties properties;

    public ExpireAwaitingPaymentOrdersJob(
            OrderRepository orderRepository,
            @Qualifier("transactionalOrderLifecycleService") OrderLifecycleService orderLifecycleService,
            SchedulerProperties properties
    ) {
        this.orderRepository = orderRepository;
        this.orderLifecycleService = orderLifecycleService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${scheduler.expire-awaiting-payment.interval-ms}")
    @SchedulerLock(name = "expire_awaiting_payment", lockAtMostFor = "30s", lockAtLeastFor = "5s")
    public void expireOrders() {
        Instant cutoff = Instant.now().minus(properties.expireAwaitingPayment().cutoff(), ChronoUnit.MINUTES);

        var expiredOrders = orderRepository.findAwaitingPaymentOlderThan(cutoff);
        if (expiredOrders.isEmpty()) {
            log.info("ExpireAwaitingPaymentOrdersJob: no expired orders");
            return;
        }

        log.info("ExpireAwaitingPaymentOrdersJob: found {} expired orders", expiredOrders.size());
        expiredOrders.forEach(order -> {
            try {
                orderLifecycleService.cancelOrder(order.getId().id());
                log.info(
                        "ExpireAwaitingPaymentOrdersJob: cancelled order={}, awaitingSince={}",
                        order.getId().id(),
                        order.getAwaitingPaymentSince()
                );
            } catch (Exception e) {
                log.error(
                        "ExpireAwaitingPaymentOrdersJob: failed to cancel order={}: {}",
                        order.getId().id(),
                        e.getMessage(),
                        e
                );
            }
        });
    }
}
