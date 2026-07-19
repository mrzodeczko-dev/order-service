package com.rzodeczko.infrastructure.adapter.http;

import com.rzodeczko.application.port.data.PaymentInitData;
import com.rzodeczko.infrastructure.configuration.properties.IntegrationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.cloud.contract.stubrunner.StubRunnerOptionsBuilder;
import org.springframework.cloud.contract.stubrunner.junit.StubRunnerExtension;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Consumer-side contract test: Order Service → Payment Service.
 */
class HttpPaymentAdapterContractTest {

    @RegisterExtension
    static StubRunnerExtension stubRunner = new StubRunnerExtension()
            .options(new StubRunnerOptionsBuilder()
                    .withStubs("com.app:payment-service")
                    .withPort(0)
                    .withStubsMode(StubRunnerProperties.StubsMode.REMOTE)
                    .withStubRepositoryRoot("https://maven.pkg.github.com/mrzodeczko-dev/payment-service")
                    .withUsername(System.getenv("GITHUB_ACTOR"))
                    .withPassword(System.getenv("GITHUB_TOKEN"))
                    .build());

    private HttpPaymentAdapter adapter;

    @BeforeEach
    void setup() {
        int port = stubRunner.findStubUrl("com.app", "payment-service").getPort();
        String baseUrl = "http://localhost:" + port;

        IntegrationProperties properties = new IntegrationProperties(
                new IntegrationProperties.Payment(baseUrl),
                new IntegrationProperties.Invoice("http://unused:0")
        );

        adapter = new HttpPaymentAdapter(RestClient.builder(), properties);
    }

    @Test
    void shouldInitPaymentAgainstStub() {
        // when
        PaymentInitData result = adapter.initPayment(
                UUID.randomUUID(),
                BigDecimal.valueOf(99.99),
                "buyer@example.com",
                "John Doe"
        );

        // then — contract guarantees: paymentId (UUID) + redirectUrl (non-blank)
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.redirectUrl()).isNotBlank();
    }

    @Test
    void shouldRefundPaymentAgainstStub() {
        // when/then — contract guarantees 200 OK, no exception
        assertThatCode(() -> adapter.refundPayment(UUID.randomUUID()))
                .doesNotThrowAnyException();
    }
}
