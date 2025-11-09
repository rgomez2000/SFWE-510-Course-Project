package com.example.order;

import com.example.order.CatalogClient;
import com.example.order.ProductSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderPlacementController {

    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final CatalogClient catalog;

    private static final SecureRandom RAND = new SecureRandom();

    public OrderPlacementController(OrderRepository orders, OrderItemRepository items, CatalogClient catalog) {
        this.orders = orders; this.items = items; this.catalog = catalog;
    }

    public record ItemReq(UUID productId, Integer qty) {}
    public record PlaceOrderReq(String orderNumber, String userId, String currency, List<ItemReq> items) {}

    @PostMapping("/place")
    public ResponseEntity<Order> place(@RequestBody PlaceOrderReq req,
                                       @RequestParam(name = "slow", defaultValue = "false") boolean slow,
                                       @RequestParam(name = "fail", defaultValue = "false") boolean fail,
                                       @AuthenticationPrincipal Jwt jwt) {

        String uid = resolveUserId(jwt);
        if (uid == null || uid.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Order o = new Order();
        o.setUserId(uid);
        o.setOrderNumber(generateUniqueOrderNo());
        o.setStatus("PENDING");
        o.setCurrency(req.currency() != null ? req.currency() : "USD");
        o.setTotalAmount(BigDecimal.ZERO);
        o = orders.save(o);

        BigDecimal total = BigDecimal.ZERO;

        if (req.items() != null) {
            for (ItemReq ir : req.items()) {
                BigDecimal price = BigDecimal.ZERO;
                String currency = "USD";
                String name = "Unavailable";

                try {
                    ProductSummary ps = catalog.getProduct(ir.productId(), slow, fail);
                    if (ps != null) {
                        if (ps.getName() != null) name = ps.getName();
                        if (ps.getPriceCurrency() != null) currency = ps.getPriceCurrency();
                        if (ps.getPriceAmount() != null) price = ps.getPriceAmount();
                    }
                } catch (Exception ignored) {
                    // Resilience demo: fallback leaves price as 0
                }

                int qty = (ir.qty() != null ? ir.qty() : 1);

                OrderItem oi = new OrderItem();
                oi.setOrder(o);
                oi.setProductId(ir.productId());
                oi.setProductNameSnapshot(name);
                oi.setUnitPriceAmountSnapshot(price);
                oi.setUnitPriceCurrencySnapshot(currency);
                oi.setQty(qty);

                BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
                oi.setLineTotalAmount(line);

                total = total.add(line);
                items.save(oi);
            }
        }

        o.setTotalAmount(total);
        o.setStatus("CREATED");
        return ResponseEntity.ok(orders.save(o));
    }

    private String resolveUserId(Jwt jwt) {
        if (jwt == null) return null;
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) return preferred;
        return jwt.getSubject(); // fallback to sub
    }

    private String randomOrderNo() {
        return "ORD-" + String.format("%08d", RAND.nextInt(100_000_000));
    }

    private String generateUniqueOrderNo() {
        String on;
        int tries = 0;
        do {
            on = randomOrderNo();
            tries++;
        } while (orders.existsByOrderNumber(on) && tries < 5);
        return on;
    }
}
