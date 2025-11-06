package com.example.order;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class CatalogClient {
    private final RestTemplate rest;
    private final String baseUrl;

    public CatalogClient(RestTemplate rest,
                         @Value("${catalog.base-url:http://catalog-service:8081}") String baseUrl) {
        this.rest = rest;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductFallback")
    @Retry(name = "catalogService", fallbackMethod = "getProductFallback")
    @RateLimiter(name = "catalogService", fallbackMethod = "getProductFallback")
    @Bulkhead(name = "catalogService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getProductFallback")
    public ProductSummary getProduct(UUID id, boolean slow, boolean fail) {
        var uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/products/{id}")
                .queryParam("slow", slow)
                .queryParam("fail", fail)
                .buildAndExpand(id)
                .toUri();
        ResponseEntity<ProductSummary> resp = rest.getForEntity(uri, ProductSummary.class);
        return resp.getBody();
    }

    public ProductSummary getProductFallback(UUID id, boolean slow, boolean fail, Throwable t) {
        ProductSummary ps = new ProductSummary();
        ps.setId(id);
        ps.setName("Unavailable");
        ps.setDescription("Fallback product");
        ps.setPriceAmount(BigDecimal.ZERO);
        ps.setPriceCurrency("USD");
        ps.setStockQty(0);
        ps.setIsActive(false);
        return ps;
    }
}