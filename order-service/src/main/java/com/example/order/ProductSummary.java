package com.example.order;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductSummary {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal priceAmount;
    private String priceCurrency;
    private Integer stockQty;
    private Boolean isActive;
    // getters/setters...
    public UUID getId(){return id;} public void setId(UUID id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getDescription(){return description;} public void setDescription(String d){this.description=d;}
    public BigDecimal getPriceAmount(){return priceAmount;} public void setPriceAmount(BigDecimal p){this.priceAmount=p;}
    public String getPriceCurrency(){return priceCurrency;} public void setPriceCurrency(String c){this.priceCurrency=c;}
    public Integer getStockQty(){return stockQty;} public void setStockQty(Integer s){this.stockQty=s;}
    public Boolean getIsActive(){return isActive;} public void setIsActive(Boolean a){this.isActive=a;}
}
