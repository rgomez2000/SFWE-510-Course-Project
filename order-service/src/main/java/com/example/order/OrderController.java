package com.example.order;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderRepository orders;

    public OrderController(OrderRepository orders){ this.orders = orders; }

    @GetMapping
    public List<Order> all(){ return orders.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOne(@PathVariable("id") UUID id) {
        return orders.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Order> create(@RequestBody Order o,
                                        @AuthenticationPrincipal Jwt jwt) {
        String uid = null;
        if (jwt != null) {
            String preferred = jwt.getClaimAsString("preferred_username");
            uid = (preferred != null && !preferred.isBlank())
                    ? preferred
                    : jwt.getSubject();
        }
        if (uid == null || uid.isBlank()) {
            uid = "system";
        }
        o.setUserId(uid);

        if (o.getTotalAmount() == null) o.setTotalAmount(BigDecimal.ZERO);


        return ResponseEntity.ok(orders.save(o));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable("id") UUID id,
                                        @RequestBody Map<String,Object> patch) {
        return orders.findById(id).map(existing -> {
            if (patch.containsKey("orderNumber")) existing.setOrderNumber((String) patch.get("orderNumber"));
            if (patch.containsKey("status")) existing.setStatus((String) patch.get("status"));
            if (patch.containsKey("currency")) existing.setCurrency((String) patch.get("currency"));
            if (patch.containsKey("totalAmount")) {
                Object v = patch.get("totalAmount");
                if (v != null) existing.setTotalAmount(new java.math.BigDecimal(String.valueOf(v)));
            }
            return ResponseEntity.ok(orders.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        if (!orders.existsById(id)) return ResponseEntity.notFound().build();
        orders.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
