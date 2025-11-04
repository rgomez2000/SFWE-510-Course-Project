package com.example.catalog.product;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import java.util.*;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Product> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Product> byId(@PathVariable UUID id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        return ResponseEntity.ok(repo.save(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable UUID id, @RequestBody Product patch) {
        return repo.findById(id).map(existing -> {
            if (patch.getName() != null) existing.setName(patch.getName());
            if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
            if (patch.getPriceAmount() != null) existing.setPriceAmount(patch.getPriceAmount());
            if (patch.getPriceCurrency() != null) existing.setPriceCurrency(patch.getPriceCurrency());
            if (patch.getStockQty() != null) existing.setStockQty(patch.getStockQty());
            if (patch.getIsActive() != null) existing.setIsActive(patch.getIsActive());
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
