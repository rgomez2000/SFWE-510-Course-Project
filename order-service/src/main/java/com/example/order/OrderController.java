package com.example.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;

    public OrderController(OrderRepository orders){ this.orders = orders; }

    @GetMapping public List<Order> all(){ return orders.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Order> byId(@PathVariable UUID id){
        return orders.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order o){
        if (o.getTotalAmount() == null) o.setTotalAmount(BigDecimal.ZERO);
        return ResponseEntity.ok(orders.save(o));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable UUID id, @RequestBody Order patch) {
        return orders.findById(id).map(existing -> {
            if (patch.getOrderNumber() != null) existing.setOrderNumber(patch.getOrderNumber());
            if (patch.getUserId() != null) existing.setUserId(patch.getUserId());
            if (patch.getStatus() != null) existing.setStatus(patch.getStatus());
            if (patch.getCurrency() != null) existing.setCurrency(patch.getCurrency());
            if (patch.getTotalAmount() != null) existing.setTotalAmount(patch.getTotalAmount());
            return ResponseEntity.ok(orders.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        if (!orders.existsById(id)) return ResponseEntity.notFound().build();
        orders.deleteById(id); return ResponseEntity.noContent().build();
    }
}
