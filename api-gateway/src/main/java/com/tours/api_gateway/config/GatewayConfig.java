package com.tours.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("http://localhost:8084"))
                .route("compras-service", r -> r.path("/api/compras/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter()))
                        .uri("http://localhost:8080"))
                .route("payment-service", r -> r.path("/api/payment/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter()))
                        .uri("http://localhost:8090"))
                .route("email-service", r -> r.path("/api/email/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter()))
                        .uri("http://localhost:8081"))
                .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }
}