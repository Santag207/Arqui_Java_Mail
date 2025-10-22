package com.example.rabbitmqdemo.controller;

import com.example.rabbitmqdemo.model.MessageDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.queue.name}")
    private String queueName;
    
    @PostMapping("/send")
    public String sendMessage(@RequestBody MessageDto message) {
        rabbitTemplate.convertAndSend(queueName, message);
        return "Mensaje enviado: " + message.getContent();
    }
}