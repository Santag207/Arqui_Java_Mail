package com.envio_correo.email.controllers;

import com.envio_correo.email.services.RabbitMQProducer;
import com.envio_correo.email.services.models.EmailMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQController {

    private final RabbitMQProducer rabbitMQProducer;

    public RabbitMQController(RabbitMQProducer rabbitMQProducer) {
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmailMessage(@RequestBody EmailMessageDTO emailMessage) {
        try {
            log.info("🌐 SOLICITUD RECIBIDA PARA ENVIAR MENSAJE A RABBITMQ");
            log.info("📨 Desde: Postman");
            log.info("👤 Destinatario: {}", emailMessage.getDestinatario());
            log.info("📧 Asunto: {}", emailMessage.getAsunto());

            rabbitMQProducer.sendEmailMessage(emailMessage);

            return ResponseEntity.ok("✅ Mensaje enviado a RabbitMQ exitosamente. El correo será procesado pronto.");

        } catch (Exception e) {
            log.error("💥 ERROR EN EL CONTROLADOR RABBITMQ: {}", e.getMessage());
            return ResponseEntity.badRequest().body("❌ Error enviando mensaje a RabbitMQ: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ Servicio RabbitMQ funcionando correctamente");
    }
}