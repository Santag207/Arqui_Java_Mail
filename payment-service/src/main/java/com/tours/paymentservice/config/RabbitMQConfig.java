package com.tours.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ======== QUEUES ========
    @Bean
    public Queue usuarioRegistradoQueue() {
        return new Queue("usuario.registrado.queue", true);
    }

    @Bean
    public Queue pagoQueue() {
        return new Queue("pago.queue", true);
    }

    // ======== EXCHANGES ========
    @Bean
    public TopicExchange usuarioExchange() {
        return new TopicExchange("usuario.exchange", true, false);
    }

    @Bean
    public TopicExchange compraExchange() {
        return new TopicExchange("compra.exchange", true, false);
    }

    // ======== BINDINGS ========
    @Bean
    public Binding usuarioRegistradoBinding(Queue usuarioRegistradoQueue, TopicExchange usuarioExchange) {
        return BindingBuilder.bind(usuarioRegistradoQueue)
                .to(usuarioExchange)
                .with("usuario.registrado");
    }

    @Bean
    public Binding pagoBinding(Queue pagoQueue, TopicExchange compraExchange) {
        return BindingBuilder.bind(pagoQueue)
                .to(compraExchange)
                .with("pago.routingkey");
    }

    // ======== MESSAGE CONVERTER ========
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}