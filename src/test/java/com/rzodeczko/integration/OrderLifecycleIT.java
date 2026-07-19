package com.rzodeczko.integration;

import com.rzodeczko.domain.model.order.OrderStatus;
import com.rzodeczko.infrastructure.persistence.entity.InventoryEntity;
import com.rzodeczko.infrastructure.persistence.entity.OrderEntity;
import com.rzodeczko.infrastructure.persistence.entity.ProductEntity;
import com.rzodeczko.infrastructure.persistence.repository.JpaInventoryRepository;
import com.rzodeczko.infrastructure.persistence.repository.JpaOrderRepository;
import com.rzodeczko.infrastructure.persistence.repository.JpaProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the full order lifecycle using Testcontainers.
 * Tests the real HTTP endpoints with a real MySQL database.
 */
class OrderLifecycleIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private RestTestClient restTestClient;

    @Autowired
    private JpaOrderRepository jpaOrderRepository;

    @Autowired
    private JpaProductRepository jpaProductRepository;

    @Autowired
    private JpaInventoryRepository jpaInventoryRepository;

    private UUID storeId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        restTestClient = RestTestClient.bindTo(mockMvc).build();
        jpaOrderRepository.deleteAll();
        jpaInventoryRepository.deleteAll();
        jpaProductRepository.deleteAll();

        storeId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        productId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        jpaProductRepository.save(ProductEntity.builder()
                .id(productId)
                .sku("TEST-PROD-001")
                .name("Test Product")
                .unitPrice(new BigDecimal("100.00"))
                .currency("PLN")
                .taxRate(new BigDecimal("23"))
                .active(true)
                .build());

        jpaInventoryRepository.save(InventoryEntity.builder()
                .id(UUID.randomUUID())
                .storeId(storeId)
                .productId(productId)
                .quantityOnHand(50)
                .quantityReserved(0)
                .build());
    }

    @Test
    void shouldCreateDraftOrder() {
        restTestClient.post().uri("/orders?storeId=" + storeId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DRAFT")
                .jsonPath("$.orderId").isNotEmpty();
    }

    @Test
    void shouldAddItemToDraftOrder() {
        // create draft
        Map<String, Object> created = restTestClient.post().uri("/orders?storeId=" + storeId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        String orderId = created.get("orderId").toString();

        // add item
        restTestClient.post().uri("/orders/" + orderId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"productId": "%s", "quantity": 2, "unitPrice": 100.00}
                        """.formatted(productId))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DRAFT");
    }

    @Test
    void shouldGetOrderById() {
        // create draft
        Map<String, Object> created = restTestClient.post().uri("/orders?storeId=" + storeId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        String orderId = created.get("orderId").toString();

        // get order
        restTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(orderId)
                .jsonPath("$.status").isEqualTo("DRAFT");
    }

    @Test
    void shouldRemoveItemFromOrder() {
        // create draft
        Map<String, Object> created = restTestClient.post().uri("/orders?storeId=" + storeId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        String orderId = created.get("orderId").toString();

        // add item
        restTestClient.post().uri("/orders/" + orderId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"productId": "%s", "quantity": 2, "unitPrice": 100.00}
                        """.formatted(productId))
                .exchange()
                .expectStatus().isOk();

        // remove item
        restTestClient.delete().uri("/orders/" + orderId + "/items?productId=" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalAmount").isEqualTo("0.00 PLN");
    }

    @Test
    void shouldReturn404ForNonExistentOrder() {
        restTestClient.get().uri("/orders/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldCancelPlacedOrder() {
        // create + add item
        Map<String, Object> created = restTestClient.post().uri("/orders?storeId=" + storeId)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class).returnResult().getResponseBody();
        String orderId = created.get("orderId").toString();

        restTestClient.post().uri("/orders/" + orderId + "/items")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"productId": "%s", "quantity": 1, "unitPrice": 100.00}
                        """.formatted(productId))
                .exchange()
                .expectStatus().isOk();

        // manually set to PLACED in DB for cancel test (bypass payment flow)
        OrderEntity orderEntity = jpaOrderRepository.findByIdWithItems(UUID.fromString(orderId)).orElseThrow();
        jpaOrderRepository.saveAndFlush(OrderEntity.builder()
                .id(orderEntity.getId())
                .storeId(orderEntity.getStoreId())
                .status(OrderStatus.PLACED)
                .totalAmount(orderEntity.getTotalAmount())
                .currency(orderEntity.getCurrency())
                .buyerEmail("buyer@example.com")
                .buyerName("John Doe")
                .buyerTaxId("1234567890")
                .items(orderEntity.getItems())
                .build());

        // cancel
        restTestClient.post().uri("/orders/" + orderId + "/cancel")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("CANCELLED");
    }

    @Test
    void shouldListAllOrders() {
        restTestClient.post().uri("/orders?storeId=" + storeId).exchange();
        restTestClient.post().uri("/orders?storeId=" + storeId).exchange();

        restTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class)
                .value(list -> assertThat(list).hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void shouldReserveStock() {
        restTestClient.post().uri("/inventories/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"storeId": "%s", "productId": "%s", "quantity": 5}
                        """.formatted(storeId, productId))
                .exchange()
                .expectStatus().isOk();

        InventoryEntity inventory = jpaInventoryRepository
                .findByStoreIdAndProductId(storeId, productId).orElseThrow();
        assertThat(inventory.getQuantityReserved()).isEqualTo(5);
    }

    @Test
    void shouldReleaseStock() {
        // reserve first
        InventoryEntity inventory = jpaInventoryRepository
                .findByStoreIdAndProductId(storeId, productId).orElseThrow();
        inventory.setQuantityReserved(10);
        jpaInventoryRepository.saveAndFlush(inventory);

        restTestClient.post().uri("/inventories/release")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"storeId": "%s", "productId": "%s", "quantity": 5}
                        """.formatted(storeId, productId))
                .exchange()
                .expectStatus().isOk();

        InventoryEntity updated = jpaInventoryRepository
                .findByStoreIdAndProductId(storeId, productId).orElseThrow();
        assertThat(updated.getQuantityReserved()).isEqualTo(5);
    }

    @Test
    void shouldReplenishStock() {
        restTestClient.post().uri("/inventories/replenish")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"storeId": "%s", "productId": "%s", "quantity": 20}
                        """.formatted(storeId, productId))
                .exchange()
                .expectStatus().isOk();

        InventoryEntity inventory = jpaInventoryRepository
                .findByStoreIdAndProductId(storeId, productId).orElseThrow();
        assertThat(inventory.getQuantityOnHand()).isEqualTo(70);
    }
}
