package com.example.subscriber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SubscriberApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriberApplication.class, args);
        System.out.println("�� SUBSCRIBER iniciado en puerto 8081");
        System.out.println("📥 Escuchando mensajes de la cola: messageQueue");
    }
}
