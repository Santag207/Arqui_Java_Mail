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
    public void consumeEmailMessage(String jsonPayload) {
        EmailMessageDTO emailMessage = null; // declaración fuera del try
        try {
            if (jsonPayload == null || jsonPayload.trim().isEmpty()) {
                log.warn("📥 Mensaje vacío recibido - se descarta");
                return; // ack y salir
            }

            // Intentar leer como árbol para detectar formato
            JsonNode root;
            try {
                root = objectMapper.readTree(jsonPayload);
            } catch (Exception ex) {
                log.warn("📥 Payload no es JSON válido: {}. Se descarta", jsonPayload);
                return; // no re-lanzar
            }

            if (root.has("destinatario") || root.has("asunto") || root.has("mensaje")) {
                // JSON directo con los campos de EmailMessageDTO
                emailMessage = objectMapper.treeToValue(root, EmailMessageDTO.class);
            } else if (root.has("content")) {
                // Publicador envía { "content": "...json string..." , "sender": "..." }
                JsonNode contentNode = root.get("content");
                if (contentNode == null || contentNode.isNull()) {
                    log.warn("📥 Campo 'content' nulo en el mensaje. Se descarta. Payload: {}", jsonPayload);
                    return; // ack y salir
                }
                String contentStr = contentNode.isTextual()
                        ? contentNode.asText()
                        : objectMapper.writeValueAsString(contentNode);

                if (contentStr == null || contentStr.trim().isEmpty() || "null".equals(contentStr.trim())) {
                    log.warn("📥 'content' vacío o 'null'. Verificar publicador. Payload: {}", jsonPayload);
                    return; // ack y salir
                }

                try {
                    emailMessage = objectMapper.readValue(contentStr, EmailMessageDTO.class);
                } catch (Exception ex) {
                    log.warn("📥 'content' no contiene EmailMessageDTO válido: {}. Se descarta", contentStr);
                    return;
                }
            } else {
                // Fallback: intentar parseo directo a DTO
                try {
                    emailMessage = objectMapper.readValue(jsonPayload, EmailMessageDTO.class);
                } catch (Exception ex) {
                    log.warn("📥 No se reconoce formato del mensaje. Se descarta: {}", jsonPayload);
                    return;
                }
            }

            log.info("📩 MENSAJE RECIBIDO DE RABBITMQ - Cola: {}", System.getProperty("rabbitmq.queue.email.name","messageQueue"));
            log.info("👤 Destinatario: {}", emailMessage.getDestinatario());
            log.info("📧 Asunto: {}", emailMessage.getAsunto());

            // Validación mínima
            if (emailMessage == null || emailMessage.getDestinatario() == null || emailMessage.getDestinatario().trim().isEmpty()) {
                log.warn("📛 Destinatario inválido. Se descarta el mensaje (no re-lanzado).");
                return; // no re-lanzar excepción
            }

            // Convertir EmailMessageDTO a EmailDTO
            EmailDTO emailDTO = new EmailDTO();
            emailDTO.setDestinatario(emailMessage.getDestinatario());
            emailDTO.setAsunto(emailMessage.getAsunto());
            emailDTO.setMensaje(emailMessage.getMensaje());

            log.info("🚀 INICIANDO ENVÍO DE CORREO DESDE RABBITMQ...");
            try {
                emailService.sendMail(emailDTO);
                log.info("✅ CORREO ENVIADO EXITOSAMENTE DESDE RABBITMQ - {}", emailMessage.getDestinatario());
            } catch (Exception sendEx) {
                // Manejar fallo de envío sin cerrar listener
                log.error("❌ Error enviando email al procesar mensaje de cola: {}", sendEx.getMessage(), sendEx);
                // opcional: guardar en tabla de errores / enviar a DLQ manual
            }

        } catch (Exception e) {
            // Captura final: evitar re-lanzar para que el listener no se detenga
            log.error("💥 Error inesperado procesando mensaje: {}", e.getMessage(), e);
            // NO re-lanzar RuntimeException aquí para evitar que el container detenga el listener
        }
    }
}