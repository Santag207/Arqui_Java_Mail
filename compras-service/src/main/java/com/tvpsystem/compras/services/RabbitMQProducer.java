package com.tvpsystem.compras.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.tvpsystem.compras.services.models.EmailMessageDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:emailExchange}")
    private String exchange;

    @Value("${rabbitmq.routing.email.key:emailRoutingKey}")
    private String emailRoutingKey;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmailMessage(EmailMessageDTO emailMessage) {
        try {
            log.info("📤 ENVIANDO NOTIFICACIÓN DE COMPRA A RABBITMQ");
            log.info("📧 Destinatario: {}", emailMessage.getDestinatario());
            log.info("📝 Asunto: {}", emailMessage.getAsunto());
            log.info("📦 Exchange: {}", exchange);
            log.info("🔑 Routing Key: {}", emailRoutingKey);
            log.info("📨 Mensaje: {}", emailMessage);

            rabbitTemplate.convertAndSend(exchange, emailRoutingKey, emailMessage);

            log.info("✅ NOTIFICACIÓN ENVIADA A COLA DE CORREOS");
            log.info("🔍 Verificar la cola '{}' en RabbitMQ Management Console (http://localhost:15672)", "messageQueue");
        } catch (Exception e) {
            log.error("💥 ERROR AL ENVIAR NOTIFICACIÓN A RABBITMQ: {}", e.getMessage(), e);
            log.error("💥 Detalles completos del error:", e);
            throw new RuntimeException("Error enviando notificación a RabbitMQ", e);
        }
    }
}