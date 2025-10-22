package com.example.subscriber.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MessageReceiver {
    
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveMessage(String jsonMessage) {
        System.out.println("🎉 === MENSAJE RECIBIDO EN SUBSCRIBER ===");
        System.out.println("📨 JSON Original: " + jsonMessage);
        
        // Parsear manualmente el JSON (forma simple)
        try {
            String content = extractValue(jsonMessage, "content");
            String sender = extractValue(jsonMessage, "sender");
            
            System.out.println("👤 Remitente: " + sender);
            System.out.println("📝 Contenido: " + content);
        } catch (Exception e) {
            System.out.println("📝 Mensaje completo: " + jsonMessage);
        }
        
        System.out.println("🕒 Timestamp: " + java.time.LocalDateTime.now());
        System.out.println("=========================================");
    }
    
    private String extractValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int start = json.indexOf(searchKey) + searchKey.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return "No encontrado";
        }
    }
}
