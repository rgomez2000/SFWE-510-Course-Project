package com.example.order;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    private final RestTemplate restTemplate;
    public CatalogClient(RestTemplate restTemplate) { this.restTemplate = restTemplate; }

    @Value("${catalog.base-url:http://catalog-service:8081}")
    private String baseUrl;

    @Value("${resilience.simulate-random-latency:false}")
    private boolean simulateRandomLatency;

    @CircuitBreaker(name = "catalogService", fallbackMethod = "getProductFallback")
    @RateLimiter(name = "catalogService", fallbackMethod = "getProductFallback")
    @Retry(name = "catalogService", fallbackMethod = "getProductFallback")
    @Bulkhead(name = "catalogService", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getProductFallback")
    public ProductSummary getProduct(UUID id, boolean slow, boolean fail) {
        if (simulateRandomLatency) {
            try { randomlyRunLong(); }
            catch (java.util.concurrent.TimeoutException te) {
                throw new RuntimeException(te);
            }
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .path("/api/products/{id}")
                .queryParam("slow", slow)
                .queryParam("fail", fail)
                .buildAndExpand(id)
                .toUriString();

        ResponseEntity<ProductSummary> resp = restTemplate.getForEntity(url, ProductSummary.class);
        return resp.getBody();
    }


    private void randomlyRunLong() throws java.util.concurrent.TimeoutException {
        int r = new Random().nextInt(3) + 1;   // 1..3
        if (r == 3) sleep();
    }
    private void sleep() throws java.util.concurrent.TimeoutException {
        try {
            log.debug("CatalogClient: simulating client-side latency");
            Thread.sleep(5000);
            throw new java.util.concurrent.TimeoutException();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
    // ---------------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private ProductSummary getProductFallback(UUID id, boolean slow, boolean fail, Throwable t) {
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

