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
        
        // Validar que la solicitud no sea nula y que tenga códigos de paquetes
        if (request == null || request.getCodigosPaquetes() == null || request.getCodigosPaquetes().isEmpty()) {
            log.error("Solicitud inválida: request nula o sin códigos de paquetes");
            ValidacionPaqueteResponse errorResponse = new ValidacionPaqueteResponse();
            errorResponse.setTodosAprobados(false);
            errorResponse.setPuedeContinuar(false);
            errorResponse.setMensaje("La solicitud debe contener una lista de códigos de paquetes");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        log.info("Recibida solicitud de validación para {} paquetes. ID Solicitud: {}", 
                 request.getCodigosPaquetes().size(), request.getIdSolicitud());
        
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
            ValidacionPaqueteResponse errorResponse = new ValidacionPaqueteResponse();
            errorResponse.setTodosAprobados(false);
            errorResponse.setPuedeContinuar(false);
            errorResponse.setMensaje("Error interno del servidor: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            boolean available = otnService.isServiceAvailable();
            if (available) {
                return ResponseEntity.ok("Servicio OTN disponible");
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Servicio OTN no disponible");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Error verificando servicio OTN: " + e.getMessage());
        }
    }
}