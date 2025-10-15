package com.envio_correo.email.controllers;

import com.envio_correo.email.services.IEmailService;
import com.envio_correo.email.services.models.EmailDTO;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final IEmailService emailService;

    public EmailController(IEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@RequestBody EmailDTO emailDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            emailService.sendMail(emailDTO);
            
            response.put("status", "success");
            response.put("message", "Correo enviado exitosamente");
            response.put("destinatario", emailDTO.getDestinatario());
            response.put("asunto", emailDTO.getAsunto());
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (MessagingException e) {
            response.put("status", "error");
            response.put("message", "Error al enviar el correo: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error interno del servidor: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}