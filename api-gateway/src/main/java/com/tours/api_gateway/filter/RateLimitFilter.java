package com.tours.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitFilter(RedisTemplate<String, Object> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            String path = exchange.getRequest().getPath().value();
            String rateLimitKey = "rate_limit:" + clientIp + ":" + path;

            try {
                Long current = redisTemplate.opsForValue().increment(rateLimitKey);
                if (current == null) {
                    // First request
                    redisTemplate.expire(rateLimitKey, 60, TimeUnit.SECONDS);
                    return chain.filter(exchange);
                }

                if (current > config.getMaxRequests()) {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }

                return chain.filter(exchange);
            } catch (Exception e) {
                // If Redis fails, allow the request
                return chain.filter(exchange);
            }
        };
    }

    public static class Config {
        private int maxRequests = 10;

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }
    }
}