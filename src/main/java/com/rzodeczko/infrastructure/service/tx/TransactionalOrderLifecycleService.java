package com.rzodeczko.infrastructure.service.tx;

import com.rzodeczko.application.dto.CreateOrderDto;
import com.rzodeczko.application.dto.OrderSummaryDto;
import com.rzodeczko.application.dto.PlaceOrderResultDto;
import com.rzodeczko.application.service.order.OrderLifecycleService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("transactionalOrderLifecycleService")
public class TransactionalOrderLifecycleService implements OrderLifecycleService {
    private final OrderLifecycleService delegate;

    public TransactionalOrderLifecycleService(
            @Qualifier("orderLifecycleServiceImpl")
            OrderLifecycleService delegate) {
        this.delegate = delegate;
    }

    @Override
    @Transactional
    public CreateOrderDto createDraft(UUID storeId) {
        return delegate.createDraft(storeId);
    }

    @Override
    public PlaceOrderResultDto placeOrder(UUID orderId, String buyerEmail, String buyerName, String buyerTaxId) {
        return delegate.placeOrder(orderId, buyerEmail, buyerName, buyerTaxId);
    }

    @Override
    public void confirmPayment(UUID orderId, UUID paymentId) {
        delegate.confirmPayment(orderId, paymentId);
    }

    @Override
    @Transactional
    public OrderSummaryDto fulfillOrder(UUID orderId) {
        return delegate.fulfillOrder(orderId);
    }

    @Override
    @Transactional
    public OrderSummaryDto cancelOrder(UUID orderId) {
        return delegate.cancelOrder(orderId);
    }

    @Override
    @Transactional
    public OrderSummaryDto moveOrderToAnotherStore(UUID orderId, UUID oldStoreId, UUID newStoreId) {
        return delegate.moveOrderToAnotherStore(orderId, oldStoreId, newStoreId);
    }
}
