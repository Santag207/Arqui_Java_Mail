package com.example.auth_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.auth_service.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class JwksController {

    private final JwtService jwtService;

    @GetMapping("/.well-known/jwks.json")
    public String getJwks() throws JsonProcessingException {
        log.info("Serving JWKS endpoint");
        
        try {
            RSAPublicKey publicKey = (RSAPublicKey) jwtService.getPublicKey();

            Map<String, String> key = new HashMap<>();
            key.put("kty", "RSA");
            key.put("kid", "auth-key");
            key.put("use", "sig");
            key.put("alg", "RS256");
            key.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()));
            key.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()));

            Map<String, Object> jwks = new HashMap<>();
            jwks.put("keys", new Object[]{key});

            ObjectMapper mapper = new ObjectMapper();
            String jwksJson = mapper.writeValueAsString(jwks);
            
            log.info("JWKS served successfully");
            return jwksJson;
            
        } catch (Exception e) {
            log.error("Error generating JWKS: {}", e.getMessage());
            throw new RuntimeException("Error generating JWKS", e);
        }
    }
}