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
    public ResponseEntity<Product> getOne(@PathVariable("id") UUID id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        return ResponseEntity.ok(repo.save(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable("id") UUID id,
                                          @RequestBody Map<String,Object> patch) {
        return repo.findById(id).map(existing -> {
            if (patch.containsKey("name")) existing.setName((String) patch.get("name"));
            if (patch.containsKey("description")) existing.setDescription((String) patch.get("description"));
            if (patch.containsKey("priceAmount")) {
                Object v = patch.get("priceAmount");
                if (v != null) existing.setPriceAmount(new java.math.BigDecimal(String.valueOf(v)));
            }
            if (patch.containsKey("priceCurrency")) existing.setPriceCurrency((String) patch.get("priceCurrency"));
            if (patch.containsKey("stockQty")) {
                Object v = patch.get("stockQty");
                if (v != null) existing.setStockQty(Integer.valueOf(String.valueOf(v)));
            }
            if (patch.containsKey("isActive")) {
                Object v = patch.get("isActive");
                if (v != null) existing.setIsActive(Boolean.valueOf(String.valueOf(v)));
            }
            return ResponseEntity.ok(repo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
