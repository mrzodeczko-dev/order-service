package com.rzodeczko.infrastructure.adapter.http;

import com.rzodeczko.application.port.InvoicePort;
import com.rzodeczko.infrastructure.adapter.http.dto.CreateInvoiceRequestDto;
import com.rzodeczko.infrastructure.adapter.http.dto.CreateInvoiceResponseDto;
import com.rzodeczko.infrastructure.adapter.http.dto.ItemRequestDto;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class HttpInvoiceAdapter implements InvoicePort {

    private final RestClient restClient;

    public HttpInvoiceAdapter(RestClient.Builder restClientBuilder, IntegrationProperties properties) {
        this.restClient = restClientBuilder
                .baseUrl(properties.invoice().url())
                .build();
    }

    @Override
    public UUID generateInvoice(
            UUID orderId,
            String taxId,
            String buyerName,
            List<InvoiceItemData> items) {
        log.info("Generating invoice. orderId={}", orderId);
        try {
            List<ItemRequestDto> itemRequests = items
                    .stream()
                    .map(i -> new ItemRequestDto(i.name(), i.quantity(), i.price()))
                    .toList();

            CreateInvoiceResponseDto response = restClient
                    .post()
                    .uri("/invoices")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateInvoiceRequestDto(orderId, taxId, buyerName, itemRequests))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new IllegalArgumentException(
                                "Invoice service rejected request. status=" + res.getStatusCode()
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new IllegalStateException(
                                "Invoice service unavailable. status=" + res.getStatusCode()
                        );
                    })
                    .body(CreateInvoiceResponseDto.class);

            if (response == null || response.invoiceId() == null) {
                throw new IllegalStateException("Invoice service returned no invoice ID for order: " + orderId);
            }

            log.info("Invoice generated. orderId={}, invoiceId={}", orderId, response.invoiceId());
            return response.invoiceId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "Communication error with invoice service. orderId=" + orderId, e
            );
        }
    }

    @Override
    public void deleteInvoice(UUID invoiceId) {
        log.warn("deleteInvoice not implemented yet. invoiceId={}", invoiceId);
    }
}
