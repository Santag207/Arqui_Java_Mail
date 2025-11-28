package com.tours.paymentservice.controllers;

import com.tours.paymentservice.cache.UserData;
import com.tours.paymentservice.services.UserEmailCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment/users")
@RequiredArgsConstructor
@Slf4j
public class UserEmailController {

    private final UserEmailCache userEmailCache;

    @PostMapping("/register-email")
    public ResponseEntity<Map<String, String>> registrarEmail(
            @RequestParam String clienteId,
            @RequestParam String email,
            @RequestParam(required = false) String cedula,
            @RequestParam(required = false) String direccion) {
        try {
            userEmailCache.registrarUsuario(clienteId, email, cedula, direccion);
            log.info("✅ USUARIO REGISTRADO: {} -> Email: {}, Cédula: {}, Dirección: {}", 
                clienteId, email, cedula, direccion);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario registrado correctamente",
                "clienteId", clienteId,
                "email", email,
                "cedula", cedula != null ? cedula : "N/A",
                "direccion", direccion != null ? direccion : "N/A"
            ));
        } catch (Exception e) {
            log.error("❌ Error registrando usuario: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error registrando usuario: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/get-email/{clienteId}")
    public ResponseEntity<Map<String, String>> obtenerEmail(@PathVariable String clienteId) {
        try {
            String email = userEmailCache.obtenerEmail(clienteId);
            return ResponseEntity.ok(Map.of(
                "clienteId", clienteId,
                "email", email
            ));
        } catch (Exception e) {
            log.error("❌ Error obteniendo email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error obteniendo email: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/get-user/{clienteId}")
    public ResponseEntity<Map<String, String>> obtenerUsuario(@PathVariable String clienteId) {
        try {
            UserData userData = userEmailCache.obtenerUsuario(clienteId);
            return ResponseEntity.ok(Map.of(
                "clienteId", userData.getClienteId(),
                "email", userData.getEmail(),
                "cedula", userData.getCedula(),
                "direccion", userData.getDireccion()
            ));
        } catch (Exception e) {
            log.error("❌ Error obteniendo usuario: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error obteniendo usuario: " + e.getMessage()
            ));
        }
    }
}
