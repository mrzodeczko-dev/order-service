package com.rzodeczko.infrastructure.persistence.mapper;


import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.entity.OrderEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEntityMapper {
    private final OrderItemEntityMapper orderItemEntityMapper;

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = OrderEntity
                .builder()
                .id(order.getId().id())
                .storeId(order.getStoreId().id())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount().amount())
                .currency(order.getTotalAmount().currency().getCurrencyCode())
                .buyerEmail(order.getBuyerEmail())
                .buyerName(order.getBuyerName())
                .buyerTaxId(order.getBuyerTaxId())
                .paymentId(order.getPaymentId())
                .invoiceId(order.getInvoiceId())
                .paymentRedirectUrl(order.getPaymentRedirectUrl())
                .awaitingPaymentSince(order.getAwaitingPaymentSince())
                .build();

        order
                .getItems()
                .forEach(item -> entity.addItem(orderItemEntityMapper.toEntity(item)));
        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        Order order = Order.restore(
                new OrderId(entity.getId()),
                new StoreId(entity.getStoreId()),
                entity.getStatus(),
                entity.getBuyerEmail(),
                entity.getBuyerName(),
                entity.getBuyerTaxId(),
                entity.getPaymentId(),
                entity.getInvoiceId(),
                entity.getPaymentRedirectUrl(),
                entity.getAwaitingPaymentSince()
        );
        entity.getItems().forEach(item -> order.addItemForRestore(orderItemEntityMapper.toDomain(item)));
        return order;
    }
}
