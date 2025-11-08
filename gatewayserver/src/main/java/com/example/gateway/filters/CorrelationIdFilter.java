package com.example.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_ID = "tmx-correlation-id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String existing = headers.getFirst(CORRELATION_ID);
        String cid = (existing != null && !existing.isBlank()) ? existing : UUID.randomUUID().toString();

        var mutated = exchange.mutate()
                .request(r -> r.headers(h -> h.set(CORRELATION_ID, cid)))
                .build();

        if (existing == null) {
            log.debug("Injected new correlation id {}", cid);
        } else {
            log.debug("Propagating existing correlation id {}", cid);
        }
        return chain.filter(mutated);
    }

    @Override
    public int getOrder() { return -1; }
}


