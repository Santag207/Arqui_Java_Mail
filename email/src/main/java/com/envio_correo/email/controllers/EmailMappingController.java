package com.envio_correo.email.controllers;

import com.envio_correo.email.services.event.UserRegisteredListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email-mapping")
@RequiredArgsConstructor
@Slf4j
public class EmailMappingController {

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerEmailMapping(@RequestBody Map<String, Object> request) {
        try {
            String clienteId = (String) request.get("clienteId");
            String email = (String) request.get("email");

            log.info("📍 Registrando mapeo de email: {} -> {}", clienteId, email);

            // Registrar en el mapeo dinámico
            UserRegisteredListener.mapearEmail(clienteId, email);

            log.info("✅ MAPEO REGISTRADO: {} -> {}", clienteId, email);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email mapeado exitosamente",
                "clienteId", clienteId,
                "email", email
            ));

        } catch (Exception e) {
            log.error("❌ Error mapeando email: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error mapeando email: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<Map<String, Object>> getEmailMapping(@PathVariable String clienteId) {
        String email = UserRegisteredListener.obtenerEmailDinamico(clienteId);
        return ResponseEntity.ok(Map.of(
            "clienteId", clienteId,
            "email", email
        ));
    }
}
