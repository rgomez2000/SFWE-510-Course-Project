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

        // Create the order shell first
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
                } catch (Exception ex) {

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
}
