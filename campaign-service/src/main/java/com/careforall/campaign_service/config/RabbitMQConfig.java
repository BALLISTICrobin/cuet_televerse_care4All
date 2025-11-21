package com.careforall.campaign_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ==========================================
    // 1. DONATION CONFIGURATION (Existing)
    // ==========================================
    public static final String QUEUE_NAME = "donation.queue";
    public static final String EXCHANGE_NAME = "donation.exchange";
    public static final String ROUTING_KEY = "donation.created";

    @Bean
    public Queue donationQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding donationBinding(@Qualifier("donationQueue") Queue queue,
                                   @Qualifier("donationExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // ==========================================
    // 2. PAYMENT CONFIGURATION (New)
    // ==========================================
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_ROUTING_KEY = "payment.updated";

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentBinding(@Qualifier("paymentQueue") Queue queue,
                                  @Qualifier("paymentExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYMENT_ROUTING_KEY);
    }

    // ==========================================
    // 3. GENERAL CONFIGURATION
    // ==========================================
    @Bean
    public MessageConverter converter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        return rabbitTemplate;
    }
}