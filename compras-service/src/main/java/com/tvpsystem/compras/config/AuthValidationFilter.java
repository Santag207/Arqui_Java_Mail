package com.tvpsystem.compras.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public class AuthValidationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8081}")
    private String authServiceUrl;

    public AuthValidationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        log.debug("🔐 AuthValidationFilter procesando: {}", path);
        
        if (path.startsWith("/compras/public") || path.equals("/health") || path.equals("/h2-console") || path.startsWith("/h2-console/")) {
            log.debug("✅ Ruta pública permitida: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);
            log.debug("🔍 Token extraído: {}", token != null ? "Presente" : "Ausente");
            
            if (token != null && validateTokenWithAuthService(token)) {
                log.info("✅ Token válido establecido en contexto de seguridad");
                // Token válido - establecer autenticación con el token
                UserDetails userDetails = User.builder()
                    .username("user")
                    .password(token)  // Almacenar el token como password
                    .roles("USER")
                    .build();
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("❌ Token inválido o ausente");
            }
            
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("❌ Error en AuthValidationFilter: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean validateTokenWithAuthService(String token) {
        try {
            String url = authServiceUrl + "/auth/validate";
            log.debug("🌐 Validando token en: {}", url);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);

            boolean isValid = response.getStatusCode().is2xxSuccessful();
            log.info("✅ Respuesta de validación: {} (200={} )", response.getStatusCode(), isValid);
            return isValid;
        } catch (Exception e) {
            log.error("❌ Error validando token: {}", e.getMessage());
            return false;
        }
    }
}
