package com.rzodeczko.infrastructure.adapter.http;


import com.rzodeczko.application.port.PaymentPort;
import com.rzodeczko.application.port.data.PaymentInitData;
import com.rzodeczko.infrastructure.adapter.http.dto.InitPaymentRequestDto;
import com.rzodeczko.infrastructure.adapter.http.dto.InitPaymentResponseDto;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Slf4j
public class HttpPaymentAdapter implements PaymentPort {
    private final RestClient restClient;

    public HttpPaymentAdapter(RestClient.Builder restClientBuilder, IntegrationProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.payment().url())
                .build();
    }

    @Override
    public PaymentInitData initPayment(
            UUID orderId,
            BigDecimal amount,
            String email,
            String name) {
        log.info("Initializing payment. orderId={}, amount={}", orderId, amount);
        try {
            InitPaymentResponseDto response = restClient
                    .post()
                    .uri("/payments/init")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InitPaymentRequestDto(orderId, amount, email, name))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new IllegalArgumentException(
                                "Payment service rejected request. status=" + res.getStatusCode()
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new IllegalStateException(
                                "Payment service unavailable. status=" + res.getStatusCode()
                        );
                    })
                    .body(InitPaymentResponseDto.class);

            if (response == null || response.paymentId() == null) {
                throw new IllegalStateException("Payment service returned no payment ID for order: " + orderId);
            }

            log.info("Payment initiated. orderId={}, paymentId={}", orderId, response.paymentId());
            return new PaymentInitData(response.paymentId(), response.redirectUrl());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (RestClientException e) {
            throw new IllegalStateException("Communication error with payment service. orderId=" + orderId, e);
        }
    }

    @Override
    public void refundPayment(UUID paymentId) {
        log.warn("refundPayment not implemented yet. paymentId={}", paymentId);
    }
}
