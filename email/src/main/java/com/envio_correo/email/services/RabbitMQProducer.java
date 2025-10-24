package com.envio_correo.email.services;

import com.envio_correo.email.services.models.EmailMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.email.key}")
    private String emailRoutingKey;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmailMessage(EmailMessageDTO emailMessage) {
        try {
            log.info("📤 ENVIANDO MENSAJE A RABBITMQ");
            log.info("📍 Exchange: {}", exchange);
            log.info("🔑 Routing Key: {}", emailRoutingKey);
            log.info("👤 Destinatario: {}", emailMessage.getDestinatario());
            log.info("📧 Asunto: {}", emailMessage.getAsunto());
            log.info("🆔 ID Transacción: {}", emailMessage.getIdTransaccion());

            rabbitTemplate.convertAndSend(exchange, emailRoutingKey, emailMessage);

            log.info("✅ MENSAJE ENVIADO EXITOSAMENTE A RABBITMQ");
            log.info("🎯 El mensaje será procesado por el consumer de correos");

        } catch (Exception e) {
            log.error("💥 ERROR AL ENVIAR MENSAJE A RABBITMQ");
            log.error("🔧 Detalles del error: {}", e.getMessage());
            throw new RuntimeException("Error enviando mensaje a RabbitMQ", e);
        }
    }
}