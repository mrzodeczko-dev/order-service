package com.rzodeczko.application.port;

import com.rzodeczko.application.port.data.PaymentInitData;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Port for payment service integration.
 */
public interface PaymentPort {
    /**
     * Initializes a payment.
     *
     * @param orderId the order ID
     * @param amount  the payment amount
     * @param email   the payer's email
     * @param name    the payer's name
     * @return payment initialization data
     */
    PaymentInitData initPayment(UUID orderId, BigDecimal amount, String email, String name);

    /**
     * Refunds a payment.
     *
     * @param paymentId the payment ID
     */
    void refundPayment(UUID paymentId);

}
