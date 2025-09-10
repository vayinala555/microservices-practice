package com.example.InventoryService.controller;

import com.example.InventoryService.entities.Inventory;
import com.example.InventoryService.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryRepository repo;

    @PostMapping
    public ResponseEntity<?> addStock(@RequestBody Inventory inv) {
        Inventory saved = repo.save(inv);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{sku}")
    public ResponseEntity<?> updateStock(@PathVariable String sku, @RequestBody int qty) {
        Inventory inv = repo.findById(sku).orElse(new Inventory("SKU1", 100));
        inv.setSku(sku);
        inv.setAvailable(qty);
        return ResponseEntity.ok(repo.save(inv));
    }

    @GetMapping
    public List<Inventory> getAll() {
        return repo.findAll();
    }
}
