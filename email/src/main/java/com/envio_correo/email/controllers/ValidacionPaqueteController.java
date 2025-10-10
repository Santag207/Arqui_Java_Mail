package com.envio_correo.email.controllers;

import com.envio_correo.email.services.IOTNService;
import com.envio_correo.email.services.models.ValidacionPaqueteRequest;
import com.envio_correo.email.services.models.ValidacionPaqueteResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/validacion")
public class ValidacionPaqueteController {

    private final IOTNService otnService;

    public ValidacionPaqueteController(IOTNService otnService) {
        this.otnService = otnService;
    }

    @PostMapping("/paquetes")
    public ResponseEntity<ValidacionPaqueteResponse> validarPaquetes(
            @RequestBody ValidacionPaqueteRequest request) {
        
        log.info("Recibida solicitud de validación para {} paquetes", 
                 request.getCodigosPaquetes().size());
        
        try {
            ValidacionPaqueteResponse response = otnService.validarMultiplesPaquetes(request);
            
            if (response.isPuedeContinuar()) {
                log.info("Validación exitosa - Puede continuar con la compra");
            } else {
                log.warn("Validación fallida - No puede continuar con la compra: {}", 
                         response.getMensaje());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error en el proceso de validación: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        boolean available = otnService.isServiceAvailable();
        if (available) {
            return ResponseEntity.ok("Servicio OTN disponible");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Servicio OTN no disponible");
        }
    }
}