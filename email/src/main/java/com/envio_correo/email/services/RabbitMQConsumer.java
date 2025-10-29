package com.envio_correo.email.services;

import com.envio_correo.email.services.models.EmailMessageDTO;
import com.envio_correo.email.services.models.EmailDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Component
public class RabbitMQConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    private final IEmailService emailService;

    public RabbitMQConsumer(IEmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.email.name}")
    public void consumeEmailMessage(EmailMessageDTO emailMessage) {
        try {
            if (emailMessage == null) {
                log.warn("📥 Mensaje nulo recibido - se descarta");
                return;
            }

            log.info("📩 MENSAJE RECIBIDO DE RABBITMQ");
            log.info("👤 Destinatario: {}", emailMessage.getDestinatario());
            log.info("📧 Asunto: {}", emailMessage.getAsunto());

            if (emailMessage.getDestinatario() == null || emailMessage.getDestinatario().trim().isEmpty()) {
                log.warn("📛 Destinatario inválido. Se descarta el mensaje.");
                return;
            }

            // Convertir EmailMessageDTO a EmailDTO y enviar
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setDestinatario(emailMessage.getDestinatario());
            emailDTO.setAsunto(emailMessage.getAsunto());
            emailDTO.setMensaje(emailMessage.getMensaje());

            log.info("🚀 INICIANDO ENVÍO DE CORREO DESDE RABBITMQ...");
            try {
                emailService.sendMail(emailDTO);
                log.info("✅ CORREO ENVIADO EXITOSAMENTE - {}", emailMessage.getDestinatario());
            } catch (Exception sendEx) {
                log.error("❌ Error enviando email desde consumidor RabbitMQ: {}", sendEx.getMessage(), sendEx);
            }
        } catch (Exception e) {
            log.error("💥 Error inesperado procesando mensaje de cola: {}", e.getMessage(), e);
        }
    }
}