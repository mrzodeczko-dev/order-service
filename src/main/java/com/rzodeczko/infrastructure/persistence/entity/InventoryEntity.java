package com.rzodeczko.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_id"}))
public class InventoryEntity {
    @Id
    private UUID id;

    @Column(name = "store_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID storeId;

    @Column(name = "product_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID productId;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    // Konflikt
    @Version
    private Long version;
}
