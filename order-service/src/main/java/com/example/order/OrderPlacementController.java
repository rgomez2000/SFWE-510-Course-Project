package com.example.order;

import com.example.order.CatalogClient;
import com.example.order.ProductSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderPlacementController {

    private final OrderRepository orders;
    private final OrderItemRepository items;
    private final CatalogClient catalog;

    public OrderPlacementController(OrderRepository orders, OrderItemRepository items, CatalogClient catalog) {
        this.orders = orders; this.items = items; this.catalog = catalog;
    }

    public record ItemReq(UUID productId, Integer qty) {}
    public record PlaceOrderReq(String orderNumber, String userId, String currency, List<ItemReq> items) {}

    @PostMapping("/place")
    public ResponseEntity<Order> place(@RequestBody PlaceOrderReq req,
                                       @RequestParam(name = "slow", defaultValue = "false") boolean slow,
                                       @RequestParam(name = "fail", defaultValue = "false") boolean fail) {

        Order o = new Order();
        o.setOrderNumber(req.orderNumber());
        o.setUserId(req.userId());
        o.setStatus("PENDING");
        o.setCurrency(req.currency() != null ? req.currency() : "USD");
        o.setTotalAmount(BigDecimal.ZERO);
        o = orders.save(o);

        BigDecimal total = BigDecimal.ZERO;
        if (req.items() != null) {
            for (ItemReq ir : req.items()) {
                ProductSummary ps = catalog.getProduct(ir.productId(), slow, fail);

                OrderItem oi = new OrderItem();
                oi.setOrder(o);
                oi.setProductId(ir.productId());
                oi.setProductNameSnapshot(ps.getName());
                oi.setUnitPriceAmountSnapshot(ps.getPriceAmount() != null ? ps.getPriceAmount() : BigDecimal.ZERO);
                oi.setUnitPriceCurrencySnapshot(ps.getPriceCurrency() != null ? ps.getPriceCurrency() : "USD");
                oi.setQty(ir.qty() != null ? ir.qty() : 1);

                BigDecimal line = oi.getUnitPriceAmountSnapshot().multiply(BigDecimal.valueOf(oi.getQty()));
                oi.setLineTotalAmount(line);

                total = total.add(line);
                items.save(oi);
            }
        }
        o.setTotalAmount(total);
        o.setStatus("CREATED");
        return ResponseEntity.ok(orders.save(o));
    }
}