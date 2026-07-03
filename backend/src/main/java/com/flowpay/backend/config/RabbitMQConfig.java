package com.flowpay.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "flowpay.events.exchange";
    public static final String QUEUE_DASHBOARD = "flowpay.dashboard.queue";
    public static final String ROUTING_KEY_DASHBOARD = "dashboard.update";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue dashboardQueue() {
        return new Queue(QUEUE_DASHBOARD, true);
    }

    @Bean
    public Binding dashboardBinding(Queue dashboardQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(dashboardQueue).to(eventsExchange).with(ROUTING_KEY_DASHBOARD);
    }
}
