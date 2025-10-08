package com.envio_correo.email.controllers;

import com.envio_correo.email.services.IEmailService;
import com.envio_correo.email.services.models.EmailDTO;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final IEmailService emailService;

    public EmailController(IEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailDTO emailDTO) {
        try {
            emailService.sendMail(emailDTO);
            return ResponseEntity.ok("Correo enviado exitosamente");
        } catch (MessagingException e) {
            return ResponseEntity.badRequest().body("Error al enviar el correo: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error interno del servidor: " + e.getMessage());
        }
    }
}