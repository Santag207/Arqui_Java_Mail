package com.example.rabbitmqdemo.service;

import com.example.rabbitmqdemo.model.MessageDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MessageReceiver {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(String jsonMessage) {
        try {
            MessageDto message = objectMapper.readValue(jsonMessage, MessageDto.class);
            
            System.out.println("=== MENSAJE RECIBIDO ===");
            System.out.println("📨 JSON Recibido: " + jsonMessage);
            System.out.println("👤 Remitente: " + message.getSender());
            System.out.println("📝 Contenido: " + message.getContent());
            System.out.println("🕒 Timestamp: " + java.time.LocalDateTime.now());
            System.out.println("=========================");
        } catch (Exception e) {
            System.err.println("Error al procesar mensaje: " + e.getMessage());
        }
    }
}