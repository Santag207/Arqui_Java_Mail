package com.envio_correo.email.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.email.name}")
    private String emailQueue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.email.key}")
    private String emailRoutingKey;

    // Bean para la cola de email ORIGINAL (se mantiene)
    @Bean
    public Queue emailQueue() {
        return new Queue(emailQueue);
    }

    // NUEVO: Bean para la cola de pagos
    @Bean
    public Queue pagoQueue() {
        return new Queue("pago.queue", true);
    }

    // Bean para el exchange ORIGINAL (se mantiene)
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchange);
    }

    // NUEVO: Bean para el exchange de compras
    @Bean
    public TopicExchange compraExchange() {
        return new TopicExchange("compra.exchange");
    }

    // Bean para binding entre exchange y cola usando routing key ORIGINAL (se mantiene)
    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(exchange())
                .with(emailRoutingKey);
    }

    // NUEVO: Binding para la cola de pagos
    @Bean
    public Binding pagoBinding() {
        return BindingBuilder
                .bind(pagoQueue())
                .to(compraExchange())
                .with("pago.routingkey");
    }

    // Bean para convertir mensajes a JSON ORIGINAL (se mantiene)
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        return factory;
    }

    // Si usas RabbitTemplate en esta app también ORIGINAL (se mantiene):
    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        org.springframework.amqp.rabbit.core.RabbitTemplate template =
                new org.springframework.amqp.rabbit.core.RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter);
        return template;
    }
}