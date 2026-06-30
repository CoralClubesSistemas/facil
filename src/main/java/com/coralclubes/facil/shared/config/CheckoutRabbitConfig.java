package com.coralclubes.facil.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CheckoutRabbitConfig {

    public static final String QUEUE_NAME = "payment.status.queue";
    public static final String EXCHANGE_NAME = "payment.events";
    public static final String ROUTING_KEY = "payment.status.changed";

    @Bean
    public Queue paymentQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(ROUTING_KEY);
    }
}
