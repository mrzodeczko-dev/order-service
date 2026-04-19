package com.rzodeczko.presentation.controller;


import com.rzodeczko.application.service.inventory.InventoryService;
import com.rzodeczko.presentation.dto.request.ReleaseStockRequestDto;
import com.rzodeczko.presentation.dto.request.ReplenishStockRequestDto;
import com.rzodeczko.presentation.dto.request.ReserveStockRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveStockRequestDto request) {
        inventoryService.reserve(request.storeId(), request.productId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> release(@Valid @RequestBody ReleaseStockRequestDto request) {
        inventoryService.release(request.storeId(), request.productId(), request.quantity());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/replenish")
    public ResponseEntity<Void> replenish(@Valid @RequestBody ReplenishStockRequestDto request) {
        inventoryService.replenish(request.storeId(), request.productId(), request.quantity());
        return ResponseEntity.ok().build();
    }
}
