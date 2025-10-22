package com.example.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PublisherApplication {
    public static void main(String[] args) {
        SpringApplication.run(PublisherApplication.class, args);
        System.out.println("🚀 PUBLICADOR iniciado en: http://localhost:8080");
        System.out.println("📤 Listo para enviar mensajes a RabbitMQ");
    }
}
