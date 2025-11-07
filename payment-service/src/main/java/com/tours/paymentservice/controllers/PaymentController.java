package com.tours.paymentservice.controllers;

import com.tours.paymentservice.services.PaymentService;
import com.tours.paymentservice.services.dto.PaymentRequestDTO;
import com.tours.paymentservice.services.dto.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/validate")
    public ResponseEntity<PaymentResponseDTO> validatePayment(@RequestBody PaymentRequestDTO request) {
        try {
            log.info("Solicitud de validación de pago recibida: {}", request);
            PaymentResponseDTO resp = paymentService.validarYReservar(request);
            
            // SIEMPRE devolver 200 OK, el estado va en la respuesta
            return ResponseEntity.ok(resp);
            
        } catch (Exception e) {
            log.error("Error interno procesando pago: ", e);
            PaymentResponseDTO errorResp = new PaymentResponseDTO();
            errorResp.setAprobado(false);
            errorResp.setMensaje("Error interno del servidor: " + e.getMessage());
            return ResponseEntity.ok(errorResp); // También 200 para errores internos
        }
    }
}