package com.example.publisher.controller;

import com.example.publisher.model.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.queue.name}")
    private String queueName;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageDto message) {
        try {
            System.out.println("📤 PUBLICADOR - Enviando mensaje: " + message);
            
            // Convertir a JSON String
            String jsonMessage = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(queueName, jsonMessage);
            
            return "Mensaje enviado correctamente: " + message.getContent();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar mensaje: " + e.getMessage();
        }
    }
}
