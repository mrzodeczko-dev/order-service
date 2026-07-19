package com.rzodeczko.infrastructure.persistence.mapper;

import com.rzodeczko.domain.model.order.Order;
import com.rzodeczko.domain.model.order.OrderItem;
import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.domain.valueobject.Money;
import com.rzodeczko.domain.valueobject.OrderId;
import com.rzodeczko.domain.valueobject.ProductId;
import com.rzodeczko.domain.valueobject.StoreId;
import com.rzodeczko.infrastructure.persistence.entity.OrderEntity;
import com.rzodeczko.infrastructure.persistence.entity.OrderItemEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEntityMapperTest {

    private final OrderItemEntityMapper itemMapper = new OrderItemEntityMapper();
    private final OrderEntityMapper mapper = new OrderEntityMapper(itemMapper);

    @Test
    void shouldMapDomainToEntity() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        Instant awaitingSince = Instant.now();

        Order order = Order.restore(
                new OrderId(orderId), new StoreId(storeId), OrderStatus.PAID,
                "buyer@test.com", "Buyer", "TAX-1",
                paymentId, invoiceId, "http://redirect", awaitingSince
        );
        UUID productId = UUID.randomUUID();
        order.addItemForRestore(new OrderItem(new ProductId(productId), 2,
                new Money(BigDecimal.TEN, Currency.getInstance("PLN"))));

        // when
        OrderEntity entity = mapper.toEntity(order);

        // then
        assertThat(entity.getId()).isEqualTo(orderId);
        assertThat(entity.getStoreId()).isEqualTo(storeId);
        assertThat(entity.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(entity.getBuyerEmail()).isEqualTo("buyer@test.com");
        assertThat(entity.getBuyerName()).isEqualTo("Buyer");
        assertThat(entity.getBuyerTaxId()).isEqualTo("TAX-1");
        assertThat(entity.getPaymentId()).isEqualTo(paymentId);
        assertThat(entity.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(entity.getPaymentRedirectUrl()).isEqualTo("http://redirect");
        assertThat(entity.getAwaitingPaymentSince()).isEqualTo(awaitingSince);
        assertThat(entity.getItems()).hasSize(1);
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderEntity entity = OrderEntity.builder()
                .id(orderId)
                .storeId(storeId)
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .currency("PLN")
                .build();

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .productId(productId)
                .quantity(1)
                .unitPrice(BigDecimal.TEN)
                .currency("PLN")
                .build();
        entity.addItem(itemEntity);

        // when
        Order domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getId().id()).isEqualTo(orderId);
        assertThat(domain.getStoreId().id()).isEqualTo(storeId);
        assertThat(domain.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(domain.getItems()).hasSize(1);
        assertThat(domain.getItems().getFirst().getProductId().id()).isEqualTo(productId);
    }

    @Test
    void shouldMapEntityWithNullOptionalFieldsToDomain() {
        // given
        OrderEntity entity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .storeId(UUID.randomUUID())
                .status(OrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .currency("PLN")
                .build();

        // when
        Order domain = mapper.toDomain(entity);

        // then
        assertThat(domain.getBuyerEmail()).isNull();
        assertThat(domain.getPaymentId()).isNull();
        assertThat(domain.getInvoiceId()).isNull();
        assertThat(domain.getItems()).isEmpty();
    }
}
