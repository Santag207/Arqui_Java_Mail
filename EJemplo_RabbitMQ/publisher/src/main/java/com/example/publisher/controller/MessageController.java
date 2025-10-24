package com.example.publisher.controller;

import com.example.publisher.model.EmailMessageDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name:emailExchange}")
    private String exchange;

    @Value("${rabbitmq.routing.email.key:emailRoutingKey}")
    private String routingKey;

    public MessageController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostMapping("/send-direct")
    public ResponseEntity<String> sendDirect(@RequestBody EmailMessageDTO emailMessage) {
        rabbitTemplate.convertAndSend(exchange, routingKey, emailMessage);
        return ResponseEntity.ok("Enviado directo al queue");
    }

    // ...existing endpoints...
}
