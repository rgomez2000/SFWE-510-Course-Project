package com.example.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id @GeneratedValue private UUID id;

    @ManyToOne(optional = false) @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "unit_price_amount_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPriceAmountSnapshot;

    @Column(name = "unit_price_currency_snapshot", length = 3, nullable = false)
    private String unitPriceCurrencySnapshot = "USD";

    @Column(nullable = false)
    private Integer qty;

    @Column(name = "line_total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal lineTotalAmount;

    // getters/setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductNameSnapshot() { return productNameSnapshot; }
    public void setProductNameSnapshot(String v) { this.productNameSnapshot = v; }
    public BigDecimal getUnitPriceAmountSnapshot() { return unitPriceAmountSnapshot; }
    public void setUnitPriceAmountSnapshot(BigDecimal v) { this.unitPriceAmountSnapshot = v; }
    public String getUnitPriceCurrencySnapshot() { return unitPriceCurrencySnapshot; }
    public void setUnitPriceCurrencySnapshot(String v) { this.unitPriceCurrencySnapshot = v; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public BigDecimal getLineTotalAmount() { return lineTotalAmount; }
    public void setLineTotalAmount(BigDecimal v) { this.lineTotalAmount = v; }
}
