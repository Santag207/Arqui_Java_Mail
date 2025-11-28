package com.example.auth_service.service;

import com.example.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSyncService {

    @Value("${payment.service.url:http://localhost:8090}")
    private String paymentServiceUrl;

    private final RestTemplate restTemplate;

    public void sincronizarUsuarioConPaymentService(String clienteId, User user) {
        try {
            String url = paymentServiceUrl + "/api/payment/users/register-email?clienteId=" + clienteId 
                + "&email=" + user.getEmail()
                + "&cedula=" + (user.getCedula() != null ? user.getCedula() : "")
                + "&direccion=" + (user.getDireccion() != null ? user.getDireccion() : "");
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ USUARIO SINCRONIZADO CON PAYMENT-SERVICE: {} -> Email: {}, Cédula: {}, Dirección: {}", 
                    clienteId, user.getEmail(), user.getCedula(), user.getDireccion());
            } else {
                log.warn("⚠️ Respuesta inesperada de payment-service: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error sincronizando usuario con payment-service: {}", e.getMessage());
            throw new RuntimeException("No se pudo sincronizar usuario con payment-service", e);
        }
    }
}
