package com.rzodeczko.infrastructure.persistence.entity;

import com.rzodeczko.domain.model.order.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name="store_id", nullable = false)
    private UUID storeId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "buyer_email")
    private String buyerEmail;

    @Column(name = "buyer_name")
    private String buyerName;

    @Column(name = "buyer_tax_id")
    private String buyerTaxId;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    @Column(name = "payment_redirect_url")
    private String paymentRedirectUrl;

    @Column(name = "awaiting_payment_since")
    private Instant awaitingPaymentSince;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    public OrderEntity() {}

    public OrderEntity(
            UUID id,
            UUID storeId,
            OrderStatus status,
            BigDecimal totalAmount,
            String currency,
            String buyerEmail,
            String buyerName,
            String buyerTaxId,
            UUID paymentId,
            UUID invoiceId,
            String paymentRedirectUrl,
            Instant awaitingPaymentSince,
            List<OrderItemEntity> items
            ) {
        this.id = id;
        this.storeId = storeId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.buyerEmail = buyerEmail;
        this.buyerName = buyerName;
        this.buyerTaxId = buyerTaxId;
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.paymentRedirectUrl = paymentRedirectUrl;
        this.awaitingPaymentSince = awaitingPaymentSince;
        this.items = items != null ? items : new ArrayList<>();
    }
}
