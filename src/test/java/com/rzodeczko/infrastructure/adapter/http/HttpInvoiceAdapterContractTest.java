package com.rzodeczko.infrastructure.adapter.http;

import com.rzodeczko.application.port.data.InvoiceItemData;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer-side contract test: Order Service -> Invoice Service
 */
class HttpInvoiceAdapterContractTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .options(new StubRunnerOptionsBuilder()
                    .withStubs("com.rzodeczko:invoice-service")
                    .withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
                    .withStubRepositoryRoot("https://maven.pkg.github.com/mrzodeczko-dev/invoice-service")
                    .withUsername(System.getenv("GITHUB_ACTOR"))
                    .withPassword(System.getenv("GITHUB_TOKEN"))
                    .build());

    private HttpInvoiceAdapter adapter;

    @BeforeEach
    void setup() {
        int port = stubRunner.findStubUrl("com.rzodeczko", "invoice-service").getPort();
        String baseUrl = "http://localhost:" + port;

        IntegrationProperties properties = new IntegrationProperties(
                new IntegrationProperties.Payment("http://unused:0"),
                new IntegrationProperties.Invoice(baseUrl)
        );

        var requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build());
        adapter = new HttpInvoiceAdapter(RestClient.builder().requestFactory(requestFactory), properties);
    }

    @Test
    void shouldGenerateInvoiceAgainstStub() {
        // given
        List<InvoiceItemData> items = List.of(
                new InvoiceItemData("Gaming Laptop", 1, new BigDecimal("4999.99"), new BigDecimal("23"))
        );

        // when
        UUID invoiceId = adapter.generateInvoice(
                UUID.randomUUID(),
                "PL1234567890",
                "John Doe",
                items
        );

        // then contract guarantees: invoiceId (UUID) in response
        assertThat(invoiceId).isNotNull();
    }
}
